# AgentOps Platform

> 一体化 Agent 管理平台 — 以空间为业务资源容器，管理空间内模型、Agent、运行时、Prompt、Skill、工具，以及平台级用户和系统设置。

AgentOps 提供从 Agent 创建到运行、监控、优化的全生命周期管理能力，内置 MCP 协议支持与 Function Call 工具集成，帮助企业构建统一的 AI Agent 基础设施。

---

## 功能特性

| 层级 | 模块 | 描述 |
|------|------|------|
| **平台级** | **空间管理** | 多租户空间、成员管理、资源隔离、配额控制 |
| **平台级** | **用户管理** | 用户认证、角色权限、操作审计 |
| **平台级** | **系统设置** | 全局策略、默认配置、密钥策略、审计策略 |
| **空间内** | **模型管理** | LLM 模型供应商配置、API Key 管理、参数预设、健康监控 |
| **空间内** | **Agent 管理** | Agent 创建、配置、版本管理、生命周期管理 |
| **空间内** | **Agent 运行时** | Agent 执行引擎、状态监控、日志追踪、异常处理 |
| **空间内** | **Prompt 管理** | Prompt 模板管理、版本控制、变量注入、A/B 测试 |
| **空间内** | **Skill 管理** | Skill 定义、注册、绑定到 Agent |
| **空间内** | **工具管理** | MCP 协议工具、Function Call 工具、工具注册与发现 |

---

## 技术栈

### 前端
- **框架**: React 18 + TypeScript
- **构建**: Vite
- **状态管理**: Zustand
- **路由**: React Router v6
- **国际化**: react-i18next

### 后端
- **语言**: Java 17
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.x
- **缓存**: Redis
- **消息**: RabbitMQ / RocketMQ

### 架构
六层架构（Domain → Application → Adapter → Infrastructure → Client → Facade）。平台级领域包含空间管理、用户管理、系统设置；空间内领域包含模型、Agent、运行时、Prompt、Skill、工具。

---

## 快速开始

### 前置要求

- Node.js 18+
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Docker (可选，用于容器化部署)

### 本地开发

```bash
# 1. 初始化数据库
# (需本地安装 MySQL 8.0+)

# 2. 启动后端
cd server
mvn clean install -DskipTests
mvn spring-boot:run -pl agentops-adapter

# 4. 启动前端
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

---

## 项目结构

```
agentops/
├── frontend/          # React 前端
│   └── src/pages/
│       ├── spaces/           # 空间内资源：dashboard/models/agents/runtime/prompts/skills/tools
│       ├── users/            # 平台级用户管理
│       └── system-settings/  # 平台级系统设置
├── server/            # Java Spring Boot 后端（多模块 Maven）
│   ├── agentops-common/         # 公共模块
│   ├── agentops-domain/         # 领域层（实体、仓库接口）
│   ├── agentops-application/    # 应用层（用例编排）
│   ├── agentops-infrastructure/ # 基础设施（MySQL、Redis 实现）
│   ├── agentops-adapter/        # 适配层（HTTP 控制器）
│   ├── agentops-client/         # 客户端（对外 DTO）
│   ├── agentops-facade/         # 外观层（聚合服务）
│   └── agentops-starters/       # 按领域的 Starter
│       ├── agentops-starter-system/
│       ├── agentops-starter-model/
│       ├── agentops-starter-agent/
│       ├── agentops-starter-runtime/
│       ├── agentops-starter-prompt/
│       ├── agentops-starter-skill/
│       ├── agentops-starter-tool/
│       ├── agentops-starter-space/
│       └── agentops-starter-user/
```

## 六层架构

```
adapter → application → domain
                         domain ← infrastructure
                         application → client
client ← facade
```

- **Domain**: 纯业务模型，零框架依赖
- **Application**: 用例编排，事务管理
- **Infrastructure**: 数据库、缓存、消息队列实现
- **Adapter**: REST API、WebSocket、MQ 消费者
- **Client**: 对外 SDK / Feign 接口
- **Facade**: 前端专属聚合接口

---

## 贡献指南

1. Fork 项目
2. 创建功能分支: `git checkout -b feat/your-feature`
3. 提交变更: `git commit -m 'feat: add some feature'`
4. 推送到分支: `git push origin feat/your-feature`
5. 创建 Pull Request

### Git 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` 新功能
- `fix:` 修复
- `docs:` 文档
- `refactor:` 重构
- `test:` 测试
- `chore:` 构建/工具

---

## License

MIT License