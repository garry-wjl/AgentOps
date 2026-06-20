---
name: impl-frontend-page
description: Implements frontend API client files and React page components for a new domain module. Creates api/*.ts with DTOs and request functions, and pages/spaces/<module>/ with list, edit, and detail pages following Ant Design conventions. Use when adding frontend support for a new backend module after the technical solution is implemented.
---

# Implement Frontend Page Skill

Creates frontend API client and page components for a new domain module in the AgentOps platform.

## When to Use

- After implementing a new backend module (domain, application, adapter)
- When adding frontend pages for a new management feature
- User requests "add frontend for X module" or "create X management pages"

## Prerequisites

- Backend API endpoints are implemented and running
- Technical solution document exists for the module
- Existing page patterns in `frontend/src/pages/spaces/` for reference

## Workflow

### Step 1: Analyze Technical Solution

Read the technical solution document to understand:
- Entity fields and types
- API endpoints (CRUD operations)
- Status values and transitions
- Relationships with other modules

### Step 2: Create API Client File

Create `frontend/src/api/<module>.ts`:

```typescript
import { request } from '@/utils/request';
import type { PageWrapper } from '@/api/space';

// Status enum
export type <Module>Status = 'DRAFT' | 'ACTIVE' | 'DISABLED';

// DTO interface
export interface <Module>DTO {
  num: string;
  spaceCode: string;
  name: string;
  // ... other fields from technical solution
  status: <Module>Status;
  remark?: string;
  createTime: string;
  updateTime: string;
}

// Request/Response types
export interface Create<Module>Param {
  spaceCode: string;
  name: string;
  // ... required fields
}

export interface Update<Module>Param extends Create<Module>Param {
  num: string;
}

// API functions
export async function list<Module>s(spaceCode: string, page: number, size: number) {
  return request.get<PageWrapper<<Module>DTO>>('/api/<module>/list', {
    params: { spaceCode, page, size }
  });
}

export async function get<Module>(num: string) {
  return request.get<<Module>DTO>(`/api/<module>/${num}`);
}

export async function create<Module>(param: Create<Module>Param) {
  return request.post('/api/<module>', param);
}

export async function update<Module>(param: Update<Module>Param) {
  return request.put('/api/<module>', param);
}

export async function delete<Module>(num: string) {
  return request.delete(`/api/<module>}/${num}`);
}
```

### Step 3: Create List Page

Create `frontend/src/pages/spaces/<module>/<Module>ManagementPage.tsx`:

Follow conventions from existing pages (ModelManagementPage, SkillManagementPage):
- Use `ProTable` or `Table` with pagination
- 编码 (num) column always first, pinned left
- 操作 column pinned right
- Status shown as colored `Tag`
- Actions: 编辑, 删除 (with Popconfirm)

### Step 4: Create Edit Page

Create `frontend/src/pages/spaces/<module>/<Module>EditPage.tsx`:

- Use `Form` with validation
- Load existing data for edit mode
- Handle both create and update operations
- Redirect to list on success

### Step 5: Create Detail Page (if needed)

Create `frontend/src/pages/spaces/<module>/<Module>DetailPage.tsx`:

- Display all fields in read-only format
- Show status with appropriate styling
- Provide edit button to navigate to edit page

### Step 6: Update Router

Add routes to `frontend/src/App.tsx` or `SpaceLayout.tsx`:

```tsx
<Route path=":spaceCode/<module>s" element={<<Module>ManagementPage />} />
<Route path=":spaceCode/<module>s/create" element={<<Module>EditPage />} />
<Route path=":spaceCode/<module>s/:num/edit" element={<<Module>EditPage />} />
<Route path=":spaceCode/<module>s/:num" element={<<Module>DetailPage />} />
```

### Step 7: Update Navigation

Add menu item to `frontend/src/layouts/SpaceLayout.tsx` in the resources section.

## Conventions

- **Naming**: PascalCase for components, camelCase for functions
- **File structure**: `api/<module>.ts`, `pages/spaces/<module>/<Module>*.tsx`
- **Status colors**: DRAFT=blue, ACTIVE=green, DISABLED=gray, WITHDRAWN=red
- **Error handling**: Use `message.success()` / `message.error()` for feedback
- **Loading states**: Show `Spin` or `Skeleton` during data fetch

## Reference

- Existing pages: `frontend/src/pages/spaces/models/ModelManagementPage.tsx`
- API pattern: `frontend/src/api/space.ts`
- Router setup: `frontend/src/App.tsx`
