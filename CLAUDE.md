# AgentOps Platform — CLAUDE.md

## Project Overview

AgentOps 是一体化 Agent 管理平台，提供模型管理、Agent 全生命周期管理、运行时引擎、提示词 (Prompt) 管理、Skill 管理、工具 (MCP / Function Call) 管理、多空间隔离以及用户权限管理能力。

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + TypeScript + Vite |
| State Management | Zustand |
| Routing | React Router v6 |
| Backend | Java 17 + Spring Boot 3.x |
| Build Tool | Maven (multi-module) |
| Database | MySQL 8.x |
| ORM | MyBatis-Plus |
| Cache | Redis |
| Message Queue | RabbitMQ / RocketMQ |

## Project Structure

```
agentops/
├── frontend/                        # React + TypeScript
│   ├── public/
│   ├── src/
│   │   ├── api/                     # Axios API client（按领域拆：agent/model/prompt/sandbox/skill/space/system/tool/user/auth.ts）
│   │   ├── assets/                  # Static assets
│   │   ├── components/              # Shared UI components（如 PageBreadcrumb）
│   │   ├── hooks/                   # Custom React hooks
│   │   ├── layouts/                 # Layout components（PlatformLayout、SpaceLayout、BrandHeader）
│   │   ├── locales/                 # i18n (zh-CN, en-US)
│   │   ├── pages/                   # Page components
│   │   │   ├── Login/               # 登录
│   │   │   ├── Forbidden/           # 403
│   │   │   ├── platform/            # 平台级（SpaceLayout 之外）
│   │   │   │   ├── workbench/       # 平台工作台
│   │   │   │   ├── spaces/          # 空间管理（SpaceListPage + SpaceEditPage）
│   │   │   │   ├── users/           # 用户管理
│   │   │   │   └── system-settings/ # 系统设置
│   │   │   └── spaces/              # 空间内（带 :spaceId 的子页面）
│   │   │       ├── agents/          # Agent 管理（Management + Edit）
│   │   │       ├── models/          # 模型管理（Management + Edit）
│   │   │       ├── prompts/         # Prompt 管理（Management + Detail + Edit）
│   │   │       ├── sandboxes/       # 沙箱管理（Management + Edit）
│   │   │       ├── skills/          # Skill 管理（Management + Edit）
│   │   │       └── tools/           # 工具管理（Management + Edit + Test）
│   │   ├── routes/                  # Route config (React Router)
│   │   ├── stores/                  # Zustand stores（authStore、spaceResourceStore）
│   │   ├── types/                   # TypeScript definitions
│   │   └── utils/                   # Utility functions（request、listCell）
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── server/                          # Java Spring Boot
│   ├── pom.xml                      # Multi-module parent POM
│   ├── agentops-common/             # Shared utilities
│   │   ├── agentops-common-core/    # BaseEntity, Result, exceptions
│   │   └── agentops-common-web/     # Global handler, response wrapper
│   ├── agentops-domain/             # Domain entities, repository interfaces
│   ├── agentops-application/        # Application service layer
│   ├── agentops-infrastructure/     # DB, cache, MQ implementations
│   ├── agentops-adapter/            # HTTP controllers, MQ consumers
│   ├── agentops-client/             # Client SDK / RPC interfaces
│   ├── agentops-facade/             # Facade layer for frontend
│   └── agentops-starters/           # Spring Boot auto-configuration
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

## Six-Layer Architecture

### Dependency Direction
```
adapter → application → domain
                         domain ← infrastructure (implements domain repo interfaces)
                         application → client (sends events)
client ← facade (aggregates for frontend)
```

### Layer Responsibilities
- **domain**: Pure business model — entities, value objects, repository interfaces, domain services. Zero framework dependencies.
- **application**: Use case orchestration — coordinates domain services, transactions, authorization checks.
- **infrastructure**: Implements domain repository interfaces — MySQL (MyBatis-Plus), Redis cache, MQ, external HTTP/RPC clients.
- **adapter**: HTTP controllers (REST), WebSocket handlers, MQ consumers. Converts external requests into application calls.
- **client**: Feign interfaces and DTOs exposed to other (micro)services.
- **facade**: Aggregation layer — orchestrates multiple application services, assembles frontend-specific view models.

### Domain Package Convention
Each layer organizes code by domain package under `com/agentops/<layer>/`:

**Platform-level domains:**
- `space/` — Space management, members, quotas, audit scope
- `user/` — User, role & permission management
- `system/` — Platform-level settings and global policies

**Space-scoped domains:**
- `model/` — Model provider & configuration management within a space
- `agent/` — Agent management within a space
- `runtime/` — Agent runtime engine and execution records within a space
- `prompt/` — Prompt template & version management within a space
- `skill/` — Skill definition & registry within a space
- `tool/` — Tool management (MCP protocol, Function Call) within a space

All model, agent, runtime, prompt, skill, and tool data must be associated with a `spaceId`. These resources are not platform-level resources.

## Coding Conventions

### Java
- Package naming: `com.agentops.<layer>.<domain>`
- Class naming: PascalCase, entity suffixed with `Entity`, DTO suffixed with `DTO`, service suffixed with `Service`
- Repository interface follows Spring Data naming convention
- Controller methods annotated with Swagger `@Operation`
- Use Lombok `@Data`, `@Builder` for DTOs/entities to provide IDE-level quick info
- **Known limitation**: current JDK 21 + Lombok annotation processor has compatibility issue (`TypeTag :: UNKNOWN`), so explicit getter/setter methods must be kept in source code for Maven compilation. Lombok annotations still serve as IDE-level documentation.
- **Rule**: If a class uses Lombok `@Getter`/`@Setter`/`@Data`, the corresponding explicit getter/setter methods must also exist in the source file. Both must be kept in sync.
- Use MapStruct for entity ↔ DTO conversion
- Mandatory JavaDoc: every Java class/interface/enum, field/property, constructor, and method must have JavaDoc-style comments (`/** ... */`). JavaDoc comments must be written in Chinese and keep descriptions business-meaningful, not placeholder text.
- Utility-first rule: during backend development, basic utility operations such as string checks, collection checks, date handling, encoding, hashing, and common assertions must prefer Hutool utilities first (for example `StrUtil`, `CollUtil`, `DateUtil`, `Assert`, `DigestUtil`). Only use JDK APIs, custom helpers, or other libraries when Hutool does not provide a suitable utility.
- Spring bean injection rule: when injecting Spring beans, always use `@Resource` injection. Do not use constructor injection, `@Autowired`, or setter injection for Spring-managed bean dependencies unless explicitly required by a framework constraint and documented with a Chinese JavaDoc/block comment.
- MyBatis-Plus wrapper rule: database queries must use `LambdaQueryWrapper`, database update operations must use `LambdaUpdateWrapper`. Never use string-based `QueryWrapper` or `UpdateWrapper`. This avoids column name typos at compile time and improves refactoring safety.

### TypeScript / React
- Component naming: PascalCase, file matches component name
- Hooks prefix: `use*`
- Store files suffixed with `Store` (e.g. `agentStore.ts`)
- API functions grouped by domain under `api/`
- Type definitions in `types/` with `.d.ts` or `.ts` files

### SQL
- Table naming: `snake_case` plural (e.g. `agent_configs`, `space_members`)
- Column naming: `snake_case`
- Primary key: `id` (BIGINT auto_increment)
- Soft delete: `is_deleted` (TINYINT)
- Audit columns: `created_at`, `updated_at`, `created_by`, `updated_by`

## Available Skills

The following Claude Code skills are available for this project:
- `/design-technical-solution` — Technical design from PRD
- `/design-prd` — Product Requirements Document generation
- `/impl-domain-module` — Domain layer implementation
- `/impl-application-module` — Application layer implementation
- `/impl-adapter-module` — Adapter layer implementation
- `/impl-infra-module` — Infrastructure layer implementation
- `/impl-client-module` — Client module implementation
- `/impl-facade-module` — Facade layer implementation
- `/init` — Initialize/update this CLAUDE.md
- `/security-review` — Security audit

## Key Conventions

- Always use English for folder names, class names, variables, comments, and documentation
- Domain layer must NOT depend on Spring or any framework
- All cross-cutting concerns (logging, auth, rate limiting) handled in adapter layer
- Frontend-backend communication via RESTful JSON API
- Use `/` slash commands for specialized tasks

## Frontend ↔ Backend Integration Status (2026-06-16)

后端 108 个接口 ↔ 前端 108 个 API 客户端，**0 缺失 / 0 多余**。对照方式：grep 后端 `*Controller.java` 的 `@RequestMapping`+`@*Mapping` 路径集合，与 `frontend/src/api/*.ts` 中 `url:` 字符串集合做 diff。

| 领域 | 后端接口数 | 前端封装数 | 状态 |
|---|---|---|---|
| Auth | 3 | 3 | ✅ |
| Space | 9 | 9 | ✅ |
| User | 12 | 12 | ✅ |
| Model | 8 | 8 | ✅ |
| Prompt | 10 | 10 | ✅ |
| Skill（含 version / resource） | 17 | 17 | ✅ |
| Tool | 10 | 10 | ✅ |
| Agent（含 version） | 15 | 15 | ✅ |
| Sandbox | 8 | 8 | ✅ |
| System / AuditLog | 11 | 11 | ✅ |

**前端约定（涉及接口对接的页面统一遵守）：**
- **禁止使用 Ant Design `Drawer` 弹出**做新建/编辑；所有新建/编辑改为独立全页（`/new`、`/:num/edit`、详情页 `/:num`），通过 React Router 跳转
- **列表 URL 参数使用业务编码**（`:num`，对应后端 `DTO.num`）；**禁止**用 mock 时代的 `:id` 自增主键
- **删除按钮**仅在 `r.status === 'DRAFT' &&` 时渲染，与后端 `Assert.isTrue(status == DRAFT, "仅草稿态可删除")` 对齐
- **新建/编辑页顶部必须有面包屑**（用 `<PageBreadcrumb>` 组件），位于灰色背景区，**白卡外**；删除原"返回XXX列表"按钮
- **状态枚举命名**严格按后端：`Space/Model/Prompt/Skill/Tool` → `DRAFT/ENABLED|EFFECTIVE/DISABLED|WITHDRAWN`；`Sandbox` → `DRAFT/INITIALIZING/ONLINE/OFFLINE/DISABLED`；`Skill/Agent Version` → `DRAFT/ONLINE|EFFECTIVE/OFFLINE|WITHDRAWN`
- **API 函数签名**优先对象参数（`createX(spaceCode, data: { ... })`），便于扩展；`num` 类型用 `string`（业务编码），不用数字

**接口对照表**位于 `doc/2026-06-16_前后端接口对照表.md`（每次新加领域或接口时同步更新）。