---
description: Run TypeScript type checking on the frontend project. Use after frontend code changes to verify type safety before committing.
---

# TypeScript Check Command

Run TypeScript type checking on the frontend project.

## Usage

```
/tsc-check [filter]
```

## Arguments

- `$1` - Optional: filter output by keyword (e.g., "Skill", "Model", "Prompt")

## Workflow

1. **Run type check**:
   ```bash
   cd /Users/jialeiwang/claudeProject/AgentOps/frontend && \
   ./node_modules/.bin/tsc -b 2>&1; echo "EXIT=$?"
   ```

2. **With filter** (optional):
   ```bash
   cd /Users/jialeiwang/claudeProject/AgentOps/frontend && \
   npx tsc -b --pretty 2>&1 | grep -E "$FILTER" | head -20
   ```

3. **Report result**:
   - Exit code 0: "Type check passed"
   - Exit code non-zero: Show errors

## Example

```bash
# Full type check
/tsc-check

# Check only Skill-related types
/tsc-check Skill

# Check only Model-related types
/tsc-check Model
```

## Notes

- Frontend dependencies must be installed (`npm install`)
- Type errors indicate potential runtime issues
- Fix all type errors before committing
