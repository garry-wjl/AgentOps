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