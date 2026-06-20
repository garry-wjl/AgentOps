---
description: Test backend API endpoints with automatic auth token acquisition. Use when verifying API functionality, debugging endpoints, or checking backend responses after code changes.
---

# API Test Command

Test backend API endpoints with automatic authentication.

## Usage

```
/api-test <method> <path> [body]
```

## Arguments

- `$1` - HTTP method (GET, POST, PUT, DELETE)
- `$2` - API path (e.g., `/api/users`, `/api/spaces`)
- `$3` - Request body (optional, for POST/PUT)

## Workflow

1. **Acquire auth token**:
   ```bash
   TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"account":"sys@agentops.local","password":"admin123"}' \
     | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('accessToken',''))")
   ```

2. **Execute API call**:
   ```bash
   curl -s -X $METHOD http://localhost:8080$PATH \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TOKEN" \
     ${BODY:+-d "$BODY"}
   ```

3. **Format and display response**:
   ```bash
   | python3 -m json.tool
   ```

## Example

```bash
# Test GET endpoint
/api-test GET /api/spaces

# Test POST endpoint
/api-test POST /api/spaces '{"name":"Test Space","code":"test-001"}'

# Test with specific user
/api-test GET /api/users/current
```

## Notes

- Backend must be running on `localhost:8080`
- Default credentials: `sys@agentops.local` / `admin123`
- Token is cached for the session when possible
