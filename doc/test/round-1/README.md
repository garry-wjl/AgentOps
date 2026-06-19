# AgentOps 平台 — 第一轮接口自动化测试

| 测试轮次 | 日期 | 测试人 | 测试范围 |
|---------|------|--------|---------|
| Round 1 | 2026-06-14 | AgentOps Team | 全平台 8 个模块共 27 个接口冒烟测试 |

---

## 测试环境

| 组件 | 版本/配置 | 说明 |
|------|----------|------|
| 后端 | Spring Boot 3.5 + Java 17 | adapter-1.0.0-SNAPSHOT.jar (62MB fat jar) |
| 数据库 | MySQL 9.7 (Docker) | `agent_ops` 数据库，15 张表 |
| 缓存 | Redis 7.x (Docker) | |
| 测试工具 | Bash + curl + Python 3.14 | 自动化 API 测试脚本 |

---

## 测试用例清单（27 条）

### 1. 认证模块（2 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-001 | `/api/auth/login` | POST | `{"account":"sys@agentops.local","password":"admin123"}` | `code=0` | ✅ PASS |
| TC-002 | `/api/auth/current` | GET | — | `code=0` | ✅ PASS |

### 2. 空间管理（4 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-003 | `/api/spaces/create` | POST | `{"name":"客服空间","description":"客服业务","iconUrl":""}` | `code=0` + 返回 `num` | ✅ PASS |
| TC-004 | `/api/spaces/get` | GET | `?code=SP...` | `code=0` + 返回空间详情 | ✅ PASS |
| TC-005 | `/api/spaces/update-basic` | POST | `{"spaceCode":"SP...","description":"更新描述"}` | `code=0` | ✅ PASS |
| TC-006 | `/api/spaces/page-mine` | GET | `?pageNo=1` | `code=0` + 分页结果 | ✅ PASS |

### 3. 模型管理（5 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-007 | `/api/models/create` | POST | `{"spaceCode":"SP...","name":"Claude","modelId":"claude-sonnet","baseUrl":"https://api.anthropic.com/v1","apiKey":"sk-test-123"}` | `code=0` + 返回 `num` | ✅ PASS |
| TC-008 | `/api/models/enable` | POST | `{"num":"MD..."}` | `code=0` + 状态变更为 ENABLED | ✅ PASS |
| TC-009 | `/api/models/disable` | POST | `{"num":"MD..."}` | `code=0` + 状态变更为 DISABLED | ✅ PASS |
| TC-010 | `/api/models/enable` (2) | POST | `{"num":"MD..."}` | `code=0` + 禁用后重新启用 | ✅ PASS |
| TC-011 | `/api/models/list-enabled` | GET | `?spaceCode=SP...` | `code=0` + 仅返回启用态模型 | ✅ PASS |

### 4. Prompt 管理（3 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-012 | `/api/prompts/create` | POST | `{"spaceCode":"SP...","name":"开场白","key":"opening","content":"你是{{role}}，请用{{language}}回答。"}` | `code=0` + 返回 `num` | ✅ PASS |
| TC-013 | `/api/prompts/submit` | POST | `{"num":"PR..."}` | `code=0` + 草稿→启用 | ✅ PASS |
| TC-014 | `/api/prompts/list-enabled` | GET | `?spaceCode=SP...` | `code=0` + 仅返回启用态 Prompt | ✅ PASS |

### 5. 沙箱管理（4 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-015 | `/api/sandboxes/create` | POST | `{"spaceCode":"SP...","name":"默认沙箱","image":"agentops/sandbox:latest"}` | `code=0` + 返回 `num` | ✅ PASS |
| TC-016 | `/api/sandboxes/submit` | POST | `{"num":"SB..."}` | `code=0` + DRAFT→INITIALIZING | ✅ PASS |
| TC-017 | `/api/sandboxes/get` | GET | `?num=SB...` | `code=0` + 沙箱详情 | ✅ PASS |
| TC-018 | `/api/sandboxes/disable` | POST | `{"num":"SB..."}` | `code=0` + →DISABLED | ✅ PASS |

### 6. 工具管理（4 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-019 | `/api/tools/create` | POST | `{"spaceCode":"SP...","name":"天气API","type":"FUNCTION_CALL","subType":"ENDPOINT","configJson":"..."}` | `code=0` + 返回 `num` | ✅ PASS |
| TC-020 | `/api/tools/publish` | POST | `{"num":"TL..."}` | `code=0` + DRAFT→EFFECTIVE | ✅ PASS |
| TC-021 | `/api/tools/withdraw` | POST | `{"num":"TL..."}` | `code=0` + EFFECTIVE→WITHDRAWN | ✅ PASS |
| TC-022 | `/api/tools/republish` | POST | `{"num":"TL..."}` | `code=0` + WITHDRAWN→EFFECTIVE | ✅ PASS |

### 7. Skill 管理（4 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-023 | `/api/skills/create` | POST | `{"spaceCode":"SP...","name":"bank_card_validator","description":"...","tags":[],"remark":"","initialSkillMd":"..."}` | `code=0` + 返回 `num` + 初始版本 | ✅ PASS |
| TC-024 | `/api/skills/enable` | POST | `{"num":"SK..."}` | `code=0` + DRAFT→EFFECTIVE | ✅ PASS |
| TC-025 | `/api/skill-versions/publish` | POST | `{"num":"SKV..."}` | `code=0` + DRAFT→EFFECTIVE | ✅ PASS |
| TC-026 | `/api/skills/version-effective` | GET | `?skillCode=SK...` | `code=0` + 返回生效版本 | ✅ PASS |

### 8. Agent 管理（5 条）

| 用例编号 | 接口路径 | 方法 | 入参 | 预期 | 结果 |
|---------|---------|------|------|------|------|
| TC-027 | `/api/agents/create` | POST | `{"spaceCode":"SP...","name":"customer_bot","displayName":"客服","description":"...","tags":["客服"],"versionNo":"1.0.0","initialAssembly":{"modelCode":"MD...","systemPromptContent":"...","shortMemoryTurns":10}}` | `code=0` + 返回 `num` | ✅ PASS |
| TC-028 | `/api/agents/enable` | POST | `{"num":"AG..."}` | `code=0` + DRAFT→EFFECTIVE | ✅ PASS |
| TC-029 | `/api/agent-versions/pre-publish-check` | GET | `?num=AGV...` | `code=0` + 预检结果 | ✅ PASS |
| TC-030 | `/api/agent-versions/publish` | POST | `{"num":"AGV..."}` | `code=0` + DRAFT→ONLINE | ✅ PASS |
| TC-031 | `/api/agents/get-online-by-name` | GET | `?spaceCode=SP...&name=customer_bot` | `code=0` + 在线版本快照 | ✅ PASS |

---

## 测试结果

| 指标 | 值 |
|------|----|
| 总用例数 | 27（含 8 模块化分组为 31 个细分接口） |
| 通过 | **27/27（100%）** |
| 失败 | 0 |
| 阻塞 | 0 |

## 修复记录

### 修复 1：PageResult 序列化（2026-06-14 01:05）

**问题**：jackson 序列化 `PageResult<T>` 时因 Lombok 未生成 getter 方法返回 `Type definition error`，导致分页查询接口（`page-mine`、`audit-logs/page`）500 错误。

**根因**：`facade` 模块未配置 Lombok annotation processor；`PageResult` 类上的 `@Getter/@Setter` 注解未被处理，导致 Jackson 无法找到 getter。

**修复**：为 `PageResult` 类显式添加手写 getter/setter 方法，与 CLAUDE.md 项目规范保持一致。

**影响范围**：
- `/api/spaces/page-mine`
- `/api/spaces/page-members`
- `/api/models/page`
- `/api/prompts/page`
- `/api/sandboxes/page`
- `/api/skills/page`
- `/api/agents/page`
- `/api/audit-logs/page`

### 修复 2：TokenAuthInterceptor 签名变更（2026-06-13）

**问题**：`TokenProvider.createAccessToken()` 的签名由 `(Long userId, String userNum)` 变更为 `(String userCode)`，但 `AuthCommandService.login()` 和 `AuthQueryService.current()` 的调用未同步更新。

**根因**：V1.5 跨领域引用统一使用业务编码（String），全局 `Long operatorId` 改 `String operatorCode` 的重构未覆盖到 auth 模块中 `tokenProvider.createAccessToken(user.id, user.num)` 的调用。

**修复**：
- `TokenProvider.createAccessToken()` 签名改为 `(String userCode)`
- `AuthCommandService.login()` 调用改为 `tokenProvider.createAccessToken(user.num)`
- `AuthQueryService.current()` 调用 `getByNum` 替代 `getById`

### 修复 3：YAML 配置格式错误（2026-06-14 00:50）

**问题**：向 `application.yml` 追加加密密钥配置时，echo 命令将内容追加到了上一行末尾导致 YAML 解析失败，应用启动失败。

**修复**：使用正确的 YAML 缩进格式配置 `agentops.crypto.encryption-key`。

---

## 注意事项

1. 测试开始前需通过 DDL 初始化数据库（15 张表）并设置系统用户种子数据
2. 系统用户密码使用 BCrypt 哈希存储，seed 中配置的密码为 `admin123`
3. 各接口测试按照模块顺序执行，后序模块（如 Agent）依赖前序模块（如 Model）的返回值
4. 由于全程通过单一 token 认证，退出登录操作需在测试完成后执行，否则会丢失认证状态