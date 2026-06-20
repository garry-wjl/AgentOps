---
description: Clean build output and compile the server project. Use after code changes to verify compilation, before committing, or when encountering stale class files.
---

# Build Command

Clean Maven target directories and compile the server project.

## Usage

```
/build [module]
```

## Arguments

- `$1` - Optional: specific module to compile (adapter, application, client, domain, facade, infra). If omitted, compiles all modules.

## Workflow

1. **Clean all target directories**:
   ```bash
   find /Users/jialeiwang/claudeProject/AgentOps/server -type d -name target -prune -exec rm -rf {} +
   ```

2. **Compile** (all or specific module):
   ```bash
   # All modules
   mvn clean compile -f /Users/jialeiwang/claudeProject/AgentOps/server/pom.xml

   # Specific module
   mvn clean compile -f /Users/jialeiwang/claudeProject/AgentOps/server/pom.xml -pl $MODULE -am
   ```

3. **Report result**:
   - Success: "Build succeeded"
   - Failure: Show compilation errors

## Example

```bash
# Build all modules
/build

# Build only adapter module
/build adapter

# Build application module with dependencies
/build application
```

## Module Dependencies

- `adapter` depends on: application, client
- `application` depends on: client, domain, infra
- `domain` depends on: facade
- `infra` depends on: domain, facade
- `client` depends on: facade
- `facade`: no business dependencies

## Notes

- Use `-pl <module> -am` to compile a module and its dependencies
- Full build takes ~2-5 minutes depending on machine
- Check `pom.xml` for module structure changes
