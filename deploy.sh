#!/bin/bash
# MOCHU-OA 生产部署一键脚本
# 用法: ./deploy.sh [deploy|fix-db|rebuild|reset]
set -e
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
DEPLOY_DIR="$PROJECT_ROOT/deploy"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 修复数据库中的 BCrypt 密码 hash ($2b$ → $2a$)
fix_db_password() {
  log_info "修复数据库 admin 密码 hash..."
  docker exec mochu-mysql mysql -uroot -pmochu@2026 mochu_oa -e "
    UPDATE sys_user
    SET password_hash = REPLACE(password_hash, '\$2b\$', '\$2a\$')
    WHERE password_hash LIKE '\$2b\$%';
  " 2>/dev/null
  log_info "密码 hash 修复完成"
}

# 构建并部署
deploy() {
  log_info "=== 开始部署 MOCHU-OA ==="
  cd "$DEPLOY_DIR"

  log_info "1/4 构建前端和后端 Docker 镜像..."
  docker compose build --no-cache backend frontend

  log_info "2/4 停止旧容器..."
  docker compose down

  log_info "3/4 启动所有服务..."
  docker compose up -d

  log_info "等待后端健康检查通过..."
  for i in $(seq 1 60); do
    if docker exec mochu-oa-backend curl -sf http://localhost:9090/api/v1/auth/health >/dev/null 2>&1; then
      log_info "后端健康检查通过"
      break
    fi
    if [ $i -eq 60 ]; then
      log_warn "后端启动超时，查看日志: docker logs mochu-oa-backend"
    fi
    sleep 3
  done

  log_info "4/4 修复数据库密码 hash..."
  fix_db_password

  log_info "=== 部署完成 ==="
  echo ""
  echo "  访问地址: http://$(hostname -I 2>/dev/null | awk '{print $1}' || echo 'localhost'):80"
  echo "  管理员账号: admin"
  echo "  管理员密码: Admin@2026"
  echo ""
  echo "  验证登录: curl -s http://localhost/api/v1/auth/check-account -H 'Content-Type: application/json' -d '{\"account\":\"admin\"}'"
}

# 仅重建前端
rebuild_frontend() {
  log_info "仅重建前端..."
  cd "$DEPLOY_DIR"
  docker compose build --no-cache frontend
  docker compose up -d frontend
  log_info "前端重建完成"
}

# 完全重置（删除数据库卷，重新初始化）
reset() {
  log_warn "⚠ 这将删除所有数据！"
  read -p "确认重置? (y/N) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_info "已取消"
    exit 0
  fi
  cd "$DEPLOY_DIR"
  docker compose down -v
  docker compose up -d
  log_info "等待数据库初始化..."
  sleep 20
  fix_db_password
  log_info "重置完成"
}

case "${1:-deploy}" in
  deploy)
    deploy
    ;;
  fix-db)
    fix_db_password
    ;;
  rebuild)
    rebuild_frontend
    ;;
  reset)
    reset
    ;;
  *)
    echo "用法: $0 {deploy|fix-db|rebuild|reset}"
    echo "  deploy  - 完整构建并部署"
    echo "  fix-db  - 仅修复数据库密码 hash"
    echo "  rebuild - 仅重建前端"
    echo "  reset   - 完全重置（删除所有数据）"
    exit 1
    ;;
esac
