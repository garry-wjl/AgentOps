import type { UserRecord, UserRole, UserStatus } from '../types/user';

export const roleOptions: { label: UserRole; value: UserRole }[] = [
  { label: '管理员', value: '管理员' },
  { label: '普通用户', value: '普通用户' },
];

export const statusText: Record<UserStatus, string> = {
  draft: '草稿',
  enabled: '启用',
  disabled: '禁用',
};

export const statusColor: Record<UserStatus, string> = {
  draft: 'default',
  enabled: 'success',
  disabled: 'error',
};

export function isAdmin(roles: UserRole[]) {
  return roles.includes('管理员');
}

export function createBusinessCode() {
  const now = new Date();
  const pad = (value: number, length = 2) => String(value).padStart(length, '0');
  const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}${pad(now.getMilliseconds(), 3)}`;
  const random = pad(Math.floor(Math.random() * 10000), 4);
  return `US${timestamp}${random}`;
}

export const initialUsers: UserRecord[] = [
  {
    id: '1',
    businessCode: 'US202606061426301234567',
    name: '张三',
    email: 'admin@example.com',
    phone: '13800000000',
    roles: ['管理员'],
    status: 'enabled',
    remark: '平台管理员账号',
    updatedAt: '2026-06-06 14:26:30',
  },
  {
    id: '2',
    businessCode: 'US202606061501107891234',
    name: '李四',
    email: 'user@example.com',
    phone: '13900000000',
    roles: ['普通用户'],
    status: 'enabled',
    remark: '普通业务用户',
    updatedAt: '2026-06-06 15:01:10',
  },
  {
    id: '3',
    businessCode: 'US202606061518221231111',
    name: '王五',
    email: 'draft@example.com',
    phone: '13700000000',
    roles: ['普通用户'],
    status: 'draft',
    remark: '待补充资料',
    updatedAt: '2026-06-06 15:18:22',
  },
  {
    id: '4',
    businessCode: 'US202606061530118882222',
    name: '赵六',
    email: 'disabled@example.com',
    phone: '13600000000',
    roles: ['管理员', '普通用户'],
    status: 'disabled',
    remark: '临时停用',
    updatedAt: '2026-06-06 15:30:11',
  },
];
