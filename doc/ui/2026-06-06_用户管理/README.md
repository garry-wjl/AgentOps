# AgentOps 用户管理 UI 设计

本 UI 工程对应 PRD 文档：

- `doc/产品方案/2026-06-06_用户管理-PRD.md`

## 工程结构

```text
2026-06-06_用户管理/
└── web/                    # PC 端 Ant Design Pro UI 工程
    ├── package.json
    ├── index.html
    ├── vite.config.ts
    ├── tsconfig.json
    └── src/
        ├── App.tsx
        ├── main.tsx
        ├── styles.css
        ├── pages/
        │   ├── Login/
        │   ├── SpaceHome/
        │   ├── UserManagement/
        │   └── SystemSettings/
        ├── types/
        └── utils/
```

## 技术栈

PC 端采用 Ant Design Pro 技术栈：

- React 18
- TypeScript
- Vite
- Ant Design 5
- `@ant-design/pro-components`
- ProLayout
- PageContainer
- ProTable
- ModalForm / ProForm

## 主题与配色

当前整体主题采用 Ant Design 官方默认品牌蓝作为主色：

```text
#1677FF
```

登录页视觉风格参考火山引擎控制台登录体验：左侧深色科技感品牌区 + 能力说明卡片，右侧白色登录表单卡片。

通过 `ConfigProvider` 配置主题 Token：

```tsx
<ConfigProvider
  theme={{
    token: {
      colorPrimary: '#1677FF',
      borderRadius: 8,
    },
  }}
>
```

后续可切换为 Ant Design 官方色板或 default / compact / dark 主题。

## 页面清单

| 页面 | 说明 |
|------|------|
| 登录页 | 支持邮箱或手机号 + 密码登录，模拟草稿/禁用/未设置密码等异常提示 |
| 空间管理首页 | 登录成功默认进入空间管理，普通用户和管理员都可访问 |
| 用户管理页 | 仅管理员可见，包含用户列表、新增/编辑、提交、删除、启用、禁用、重置密码 |
| 系统设置页 | 仅管理员可见，展示登录安全策略配置示例 |
| 无权限页 | 普通用户访问用户管理或系统设置时展示 403 提示 |

## 登录示例账号

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 管理员 | `admin@example.com` | `12345678` | 可访问空间管理、用户管理、系统设置 |
| 普通用户 | `user@example.com` | `12345678` | 仅可访问空间管理 |
| 草稿态用户 | `draft@example.com` | 任意 | 提示账号未启用 |
| 禁用态用户 | `disabled@example.com` | 任意 | 提示账号已禁用 |
| 未设置密码用户 | `nopwd@example.com` | 任意 | 提示账号未设置密码 |

## 运行方式

```bash
cd doc/ui/2026-06-06_用户管理/web
npm install
npm run dev
```

默认访问：

```text
http://localhost:5174
```

## PRD 对齐说明

本 UI 工程覆盖 PRD 中以下能力：

- 用户登录
- 启用/禁用/草稿状态校验
- 用户业务编码展示
- 用户角色字段（字符串列表）
- 内置角色：管理员、普通用户
- 管理员可访问用户管理和系统设置
- 普通用户不可访问用户管理和系统设置
- 用户列表、角色筛选、状态筛选
- 用户新增/保存/提交/删除/启用/禁用
- 重置密码弹窗
- 备注 200 字限制
