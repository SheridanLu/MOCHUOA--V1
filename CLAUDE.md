# MOCHU-OA 施工管理系统 - 项目规范

## 项目概述

面向中小型施工企业（50-100人）的综合办公自动化系统，覆盖项目全生命周期管理。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia | Vue 3.4+, Element Plus 2.7+ |
| 移动端 | uni-app + Vue 3 | 后续阶段 |
| 后端 | Spring Boot + MyBatis-Plus + Spring Security | Spring Boot 3.x, JDK 17+ |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7.x |
| 文件存储 | MinIO | latest |
| 定时任务 | XXL-JOB | 2.4.1 |
| API 文档 | Swagger/OpenAPI | 3.0 |

## 项目结构

```
MOCHU-OAV1/
├── backend/                    # 后端 Maven 多模块
│   ├── mochu-admin/            # 启动模块（Application入口、配置文件）
│   ├── mochu-common/           # 公共模块（工具类、常量、枚举、异常、响应封装）
│   ├── mochu-framework/        # 框架模块（安全、AOP、拦截器、过滤器）
│   ├── mochu-system/           # 系统管理（sys_表：用户/角色/权限/部门/公告/审计）
│   └── mochu-business/         # 业务模块（biz_表：项目/合同/采购/物资/财务等）
├── frontend/                   # 前端 Vue 3 项目
│   └── src/
│       ├── api/                # 接口请求
│       ├── components/         # 公共组件
│       ├── composables/        # 组合式函数
│       ├── directives/         # 自定义指令（v-permission）
│       ├── layouts/            # 布局组件
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia 状态管理
│       ├── utils/              # 工具函数
│       └── views/              # 页面组件
├── deploy/                     # 部署配置
│   └── docker-compose.yml
├── docs/                       # 文档
│   ├── sql/                    # DDL 脚本
│   └── api/                    # API 文档
├── feature_list.json           # 功能清单（开发进度追踪）
├── claude-progress.txt         # Agent 进度日志
├── init.sh                     # 一键启动脚本
└── CLAUDE.md                   # 本文件
```

## 开发阶段

| 阶段 | 核心模块 | 里程碑 |
|------|---------|--------|
| Phase 0 | 登录/组织架构/权限/主页 | M0: 系统基座可用 |
| Phase 1 | 项目/合同/模板/供应商/通讯录 | M1: 项目合同流程可用 |
| Phase 2A | 采购/物资/人力资源 | M2A: 物资管理可用 |
| Phase 2B | 施工进度/变更/财务 | M2B: 项目执行全流程可用 |
| Phase 3 | 竣工/通知/案例展示 | M3: 项目闭环可用 |
| Phase 4 | 报表/审计日志 | M4: 全系统可用 |

## 代码规范

### 后端命名

- **Java 类名**：大驼峰（`ProjectController`, `ContractService`）
- **方法名/变量名**：小驼峰（`getProjectList`, `contractName`）
- **常量**：全大写下划线（`MAX_LOGIN_ATTEMPTS`）
- **数据库表名**：前缀+下划线（`sys_user`, `biz_contract`）
- **数据库字段**：小写下划线（`project_no`, `amount_with_tax`）

### 后端分层

- **Controller**：仅参数接收、校验、响应封装。不写业务逻辑。
- **Service**：业务逻辑编排，单方法不超过 80 行。接口+实现分离。
- **Mapper/DAO**：仅数据库操作，复杂 SQL 用 XML 映射。
- **Entity**：数据库实体，与表一一对应。
- **DTO**：接收前端请求参数。
- **VO**：返回前端的视图对象。

### 前端命名

- **组件文件名**：大驼峰（`ProjectList.vue`, `ContractForm.vue`）
- **CSS 类名**：短横线（`project-list`, `contract-form`）
- **页面组件**：放在 `views/` 目录
- **公共组件**：放在 `components/` 目录
- **使用 `<script setup>` 语法**
- **Props 必须声明类型和默认值**
- **全局状态使用 Pinia**

### 事务规范

- `@Transactional` 只加在 Service 层
- 多表写操作必须开启事务
- 只读操作使用 `@Transactional(readOnly=true)`
- 事务中禁止调用第三方服务（通过事件机制异步执行）

## 接口规范

### 统一前缀

`/api/v1/`

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710489600000
}
```

### 异常响应格式

```json
{
  "code": 400,
  "message": "合同金额不能为空",
  "errors": [
    {"field": "amountWithTax", "message": "不能为空"}
  ],
  "timestamp": 1710489600000
}
```

### HTTP 状态码

| 码 | 含义 |
|----|------|
| 200 | 成功 |
| 400 | 参数错误 / 业务规则校验失败 |
| 401 | 未认证 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 数据冲突 |
| 423 | 账号锁定 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 分页参数

请求：`page`（从1开始，默认1）、`size`（默认20，最大100）

响应：`data.records`（数据列表）、`data.total`、`data.page`、`data.size`、`data.pages`

### RESTful 风格

- GET：查询
- POST：新增/提交
- PUT：全量更新
- PATCH：部分更新（状态变更等）
- DELETE：删除

## 请求头

| Header | 值 | 说明 |
|--------|---|------|
| Content-Type | application/json | 文件上传时 multipart/form-data |
| Authorization | Bearer {JWT} | 登录后所有请求必须携带 |
| X-Request-Id | UUID | 链路追踪 |
| X-Client-Type | pc / h5 / wxapp | 客户端类型 |

## 编号规则

所有业务编号使用 Redis INCR 原子自增生成，Redis 故障时切换数据库种子表（biz_no_seed）兜底。

| 实体 | 前缀 | 日期 | 序号 | 示例 | 重置 |
|------|------|------|------|------|------|
| 虚拟项目 | V | YYMM | 3位 | V2403001 | 每月 |
| 实体项目 | P | YYMMDD | 3位 | P240315001 | 每日 |
| 收入合同 | IC | YYMMDD | 2位 | IC24031501 | 每日 |
| 支出合同 | EC | YYMMDD | 2位 | EC24031501 | 每日 |
| 补充协议 | BC | YYMMDD | 2位 | BC24031501 | 每日 |
| 入库单 | RK | YYMMDD | 3位 | RK240315001 | 每日 |
| 出库单 | CK | YYMMDD | 3位 | CK240315001 | 每日 |
| 对账单 | DZ | YYMM | 2位 | DZ240301 | 每月 |
| 付款(人工) | PA | YYMMDD | 3位 | PA240315001 | 每日 |
| 付款(材料) | MP | YYMMDD | 3位 | MP240315001 | 每日 |

## 权限模型

RBAC 模型：用户 → 角色 → 权限。

- **11 个角色**：GM/PROJ_MGR/BUDGET/PURCHASE/FINANCE/LEGAL/DATA/HR/BASE/SOFT/TEAM_MEMBER
- **47 个功能权限点**：格式 `模块:操作`（如 `project:create`）
- **4 级数据权限**：全部 / 本部门 / 本人 / 指定范围
- **互斥角色**：采购员 ↔ 财务人员

## 数据库规范

- 表名前缀：`sys_`（系统表）、`biz_`（业务表）
- 所有表必须有 `created_at`、`updated_at`
- 删除使用逻辑删除（`deleted` 字段）
- 金额字段：`DECIMAL(14,2)`，单位元
- 百分比字段：`DECIMAL(5,2)`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_general_ci`
- 禁止 `SELECT *`
- WHERE 条件字段必须有索引
- 复杂 JOIN 控制在 3 表以内

## Redis 缓存 Key 规范

| 场景 | Key 格式 | TTL |
|------|---------|-----|
| 用户 Token | auth:token:{userId}:{clientType} | 30天 |
| 权限列表 | user:permissions:{userId} | 同 Token |
| 登录失败计数 | auth:login_fail:{username} | 30分钟 |
| 短信验证码 | sms:phone:{phone} | 5分钟 |
| 编号自增 | biz_no:{前缀}:{日期} | 重置周期+1天 |
| 通讯录缓存 | contact:list:{deptId} | 30分钟 |
| 待办数量 | home:todo_count:{userId} | 5分钟 |

## 审批流程通用规则

- 驳回策略：统一驳回至发起人
- 审批意见：同意≥2字符，不同意≥5字符，均为必填
- 超时处理：24h 首次提醒 → 48h 二次提醒抄送上级 → 72h 自动转办上级
- 阅办：需点击已阅确认，不阻塞流程
- 阅知：仅通知，无需操作

## 开发工作流（长周期 Agent 模式）

1. 每次会话开始：读取 `claude-progress.txt` + `git log --oneline -20` + `feature_list.json`
2. 选择最高优先级的未完成 feature
3. 运行 `init.sh` 启动开发服务器
4. 实现一个 feature（仅一个）
5. 端到端测试验证
6. 通过后：`feature_list.json` 中标记 `passes: true`
7. `git commit` 提交代码
8. 更新 `claude-progress.txt`
9. 如果上下文有余量 → 回到步骤 2

## 连接信息（本地开发）

| 服务 | 地址 | 认证 |
|------|------|------|
| MySQL | localhost:3306 | root / mochu@2026, DB: mochu_oa |
| Redis | localhost:6379 | password: mochu@2026 |
| MinIO | localhost:9000 (API) / :9001 (Console) | mochu_admin / mochu@2026 |
| XXL-JOB | localhost:8090 | admin / 123456 |
| 后端 | localhost:8080 | - |
| 前端 | localhost:5173 | - |
