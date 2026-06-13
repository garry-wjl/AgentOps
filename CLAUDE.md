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
│   │   ├── api/                     # Axios API client
│   │   ├── assets/                  # Static assets
│   │   ├── components/              # Shared UI components
│   │   ├── hooks/                   # Custom React hooks
│   │   ├── layouts/                 # Layout components
│   │   ├── locales/                 # i18n (zh-CN, en-US)
│   │   ├── pages/                   # Page components
│   │   │   ├── spaces/              # Space-level resource pages
│   │   │   │   ├── dashboard/
│   │   │   │   ├── models/
│   │   │   │   ├── agents/
│   │   │   │   ├── agent-runtime/
│   │   │   │   ├── prompts/
│   │   │   │   ├── skills/
│   │   │   │   ├── tools/
│   │   │   │   ├── members/
│   │   │   │   └── settings/
│   │   │   ├── users/               # Platform-level user management
│   │   │   └── system-settings/     # Platform-level system settings
│   │   ├── routes/                  # Route config (React Router)
│   │   ├── stores/                  # Zustand stores
│   │   ├── types/                   # TypeScript definitions
│   │   └── utils/                   # Utility functions
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