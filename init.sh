#!/bin/bash
# MOCHU-OA 开发环境一键启动脚本
# 用法: ./init.sh [start|stop|restart|status]

set -e
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$PROJECT_ROOT/deploy"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 启动 Docker 中间件
start_infra() {
  log_info "启动中间件 (MySQL/Redis/MinIO/XXL-JOB)..."
  cd "$DEPLOY_DIR"
  docker compose up -d
  log_info "等待 MySQL 就绪..."
  for i in $(seq 1 30); do
    if docker exec mochu-mysql mysqladmin ping -h localhost -uroot -pmochu@2026 &>/dev/null; then
      log_info "MySQL 就绪"
      return 0
    fi
    sleep 2
  done
  log_error "MySQL 启动超时"
  return 1
}

# 初始化 MinIO 存储桶
init_minio() {
  log_info "初始化 MinIO 存储桶..."
  sleep 3
  # 使用 mc 客户端或 curl 创建桶
  for bucket in mochu-contract mochu-project mochu-attachment mochu-export mochu-temp; do
    docker exec mochu-minio mc mb --ignore-existing local/$bucket 2>/dev/null || true
  done
  log_info "MinIO 存储桶初始化完成"
}

# 启动后端
start_backend() {
  log_info "启动后端服务..."
  cd "$BACKEND_DIR"
  if [ ! -f "mochu-admin/target/mochu-admin-1.0.0.jar" ]; then
    log_info "首次启动，编译后端项目..."
    mvn clean package -DskipTests -q
  fi
  nohup java -jar mochu-admin/target/mochu-admin-1.0.0.jar \
    --spring.profiles.active=dev \
    > "$PROJECT_ROOT/.backend.log" 2>&1 &
  echo $! > "$PROJECT_ROOT/.backend.pid"
  log_info "后端启动中 (PID: $(cat $PROJECT_ROOT/.backend.pid))，日志: .backend.log"
}

# 启动前端
start_frontend() {
  log_info "启动前端开发服务器..."
  cd "$FRONTEND_DIR"
  if [ ! -d "node_modules" ]; then
    log_info "首次启动，安装前端依赖..."
    npm install
  fi
  nohup npm run dev > "$PROJECT_ROOT/.frontend.log" 2>&1 &
  echo $! > "$PROJECT_ROOT/.frontend.pid"
  log_info "前端启动中 (PID: $(cat $PROJECT_ROOT/.frontend.pid))，访问: http://localhost:5173"
}

# 停止服务
stop_services() {
  log_info "停止服务..."
  [ -f "$PROJECT_ROOT/.backend.pid" ] && kill $(cat "$PROJECT_ROOT/.backend.pid") 2>/dev/null && rm "$PROJECT_ROOT/.backend.pid"
  [ -f "$PROJECT_ROOT/.frontend.pid" ] && kill $(cat "$PROJECT_ROOT/.frontend.pid") 2>/dev/null && rm "$PROJECT_ROOT/.frontend.pid"
  cd "$DEPLOY_DIR" && docker compose down
  log_info "所有服务已停止"
}

# 查看状态
show_status() {
  echo "=== 中间件状态 ==="
  cd "$DEPLOY_DIR" && docker compose ps
  echo ""
  echo "=== 后端状态 ==="
  if [ -f "$PROJECT_ROOT/.backend.pid" ] && kill -0 $(cat "$PROJECT_ROOT/.backend.pid") 2>/dev/null; then
    log_info "后端运行中 (PID: $(cat $PROJECT_ROOT/.backend.pid))"
  else
    log_warn "后端未运行"
  fi
  echo "=== 前端状态 ==="
  if [ -f "$PROJECT_ROOT/.frontend.pid" ] && kill -0 $(cat "$PROJECT_ROOT/.frontend.pid") 2>/dev/null; then
    log_info "前端运行中 (PID: $(cat $PROJECT_ROOT/.frontend.pid))"
  else
    log_warn "前端未运行"
  fi
}

case "${1:-start}" in
  start)
    start_infra
    start_backend
    start_frontend
    log_info "所有服务启动完成！"
    echo ""
    echo "  后端: http://localhost:8080"
    echo "  前端: http://localhost:5173"
    echo "  API文档: http://localhost:8080/doc.html"
    echo "  MinIO: http://localhost:9001"
    echo "  XXL-JOB: http://localhost:8090/xxl-job-admin"
    ;;
  stop)
    stop_services
    ;;
  restart)
    stop_services
    sleep 2
    start_infra
    start_backend
    start_frontend
    ;;
  status)
    show_status
    ;;
  *)
    echo "用法: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac
