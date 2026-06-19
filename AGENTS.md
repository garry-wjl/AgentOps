# AgentOps Platform — AI Agents Configuration

This file defines the AI agent roles configured for the AgentOps platform. Each agent role is designed for specific development and operational tasks within the project.

---

## Agent Roles

### 1. Code Review Agent

Reviews code changes for correctness, security, and adherence to platform conventions.

- **Scope**: All Java, TypeScript, and SQL changes
- **Checklist**:
  - Domain layer free of framework dependencies
  - Application layer uses domain interfaces, not infrastructure
  - Repository methods follow naming conventions
  - Proper transaction boundaries in application services
  - Frontend API calls match backend endpoints
  - Error handling follows Result/Response pattern
- **Trigger**: PR creation, `/code-review`

### 2. Documentation Agent

Generates and maintains project documentation.

- **Scope**: `docs/` directory, README updates, API documentation
- **Capabilities**:
  - Generate API docs from Spring Boot controller annotations
  - Maintain architecture decision records (ADR) in `docs/architecture/`
  - Update database schema documentation from entity definitions
  - Generate deployment guides from docker-compose configurations
- **Trigger**: `/docx`, `/design-technical-solution`

### 3. Test Generation Agent

Creates unit and integration tests.

- **Scope**: `src/test/` directories across all modules
- **Guidelines**:
  - Domain layer: pure JUnit 5 unit tests (no Spring context)
  - Application layer: JUnit 5 + Mockito for service tests
  - Adapter layer: Spring MockMvc tests
  - Infrastructure layer: Spring Boot test with testcontainers (MySQL, Redis)
  - Frontend: Vitest + React Testing Library for component tests
- **Naming**: `*Test.java` for unit tests, `*IntegrationTest.java` for integration tests

### 4. MCP Tool Development Agent

Develops and maintains MCP (Model Context Protocol) tool integrations.

- **Scope**: `server/agentops-domain/tool/`, `server/agentops-application/tool/`
- **Context**:
  - MCP protocol specification for tool discovery and execution
  - Function Call registration and schema definition
  - Tool execution logging and observability
- **Output**: ToolDefinition classes, MCP protocol handlers, tool registry services

### 5. Migration & Schema Agent

Manages database schema changes and data migrations.

- **Scope**: `docs/database/`, Flyway migration scripts
- **Rules**:
  - All schema changes via Flyway migrations (not raw SQL)
  - Backward-compatible migration design
  - Index review for new queries
  - Audit columns present on all tables
- **Location**: Migration scripts under `server/agentops-infrastructure/src/main/resources/db/migration/`

### 6. Skill Development Agent

Implements new platform skills following the six-layer architecture.

- **Scope**: All layers covering a new domain feature
- **Workflow**:
  1. Design domain entity and repository interface (`domain`)
  2. Implement repository (`infrastructure`)
  3. Create application service (`application`)
  4. Expose REST API (`adapter`)
  5. Build frontend pages (`frontend/pages/`)
  6. Add client DTOs if needed (`client`, `facade`)
- **Trigger**: `/impl-from-technical-solution`, `/design-technical-solution`

---

## Agent Communication

- All agents use English for code output (comments, logs, commit messages)
- Documentation agents may generate bilingual (zh-CN + en-US) user-facing content in `locales/`
- Agents producing code must reference `CLAUDE.md` for project conventions before writing

## Agent Constraints

- No agent should modify `CLAUDE.md` or `AGENTS.md` without explicit user request
- Domain layer agents must not introduce framework dependencies
- Frontend agents must use existing component patterns and TypeScript types
- MCP tool agents must document tool schemas and execution contracts

---

## Team Workflow Rules

### Development Pipeline (sequential, dependency-gated)

```
撰写 PRD (产品经理)
    ↓
评审 PRD (全员)
    ↓
👤 人工确认 PRD (用户)  ← 附 HTML 评审报告
    ↓
设计技术方案 (技术负责人)
    ↓
评审技术方案 (全员)
    ↓
👤 人工确认技术方案 (用户)  ← 附 HTML 评审报告
    ↓
设计测试用例 (测试工程师)
    ↓
评审测试用例 (全员)
    ↓
👤 人工确认测试用例 (用户)  ← 附 HTML 评审报告
    ↓  ┌──────────────────┐
后端开发 (后端开发师)       前端开发 (前端开发师)
    ↓  └──────────────────┘
评审后端代码 (全员)         评审前端代码 (全员)
    ↓                        ↓
👤 人工确认后端代码 (用户)  👤 人工确认前端代码 (用户)
    ↓  └──────────────────┘
审查与验证 (架构守卫 + 测试工程师)
```

### Review & Confirmation Protocol

1. **全员评审**: 每个产出（PRD/技术方案/测试用例/后端代码/前端代码）完成后，Lead 通知所有团队成员进行评审。每位成员输出评审意见（approve / change-request + 理由）。

2. **HTML 评审报告**: 全员评审通过后，Lead 汇总所有评审意见，生成 HTML 格式的评审报告。报告必须包含：
   - 评审项目名称与版本
   - 评审参与人列表
   - 逐人评审意见（含 approve/reject 状态及详细理由）
   - 汇总结论（通过 / 需修改）
   - 附件：评审对象的摘要/链接

3. **人工确认**: Lead 将 HTML 评审报告提交给用户。用户确认通过后，任务才进入下一阶段。用户未确认前，依赖该任务的所有后续任务保持 blocked 状态。

4. **修改循环**: 若评审或人工确认提出修改意见，Lead 将修改意见分配给对应负责人，修复后重新进入评审 → 人工确认循环，直至通过。

### Team Communication

- Lead 使用 `team_send_message` 分配任务和通知评审
- 评审通过后，Lead 使用 `team_task_update` 更新任务状态为 `completed`
- 人工确认任务（owner=Lead）在用户确认后由 Lead 标记为 `completed`，触发下游任务解除阻塞

---

## Iteration Progress Dashboard

### 规则

每次启动一个迭代（iteration），Lead 必须在 `doc/迭代/` 目录下创建一个 HTML 进度看板文件，命名格式为 `iteration-<编号>-progress.html`。

### HTML 进度看板要求

1. **流程图区域**: 使用 CSS/SVG 绘制完整的开发流水线流程图，每个节点对应一个任务
2. **动态状态标识**: 每个节点用颜色标识当前状态：
   - 🔵 蓝色 = pending（待开始）
   - 🟡 黄色 = in_progress（进行中）
   - 🟢 绿色 = completed（已完成）
   - 🔴 红色 = blocked（被阻塞）
3. **描述区域**: 每个节点下方或侧边显示当前任务描述、负责人、更新时间
4. **实时更新**: Lead 在每次任务状态变更时，同步更新 HTML 文件（使用 write 工具覆盖写入）
5. **自包含**: HTML 文件为单页自包含，不依赖外部资源

### 迭代生命周期

1. Lead 在迭代启动时创建 HTML 进度看板
2. Lead 在每次任务状态变更后立即更新 HTML
3. 用户可随时打开 HTML 查看最新进度
4. 迭代完成后，HTML 文件作为迭代记录归档保留