export type UserStatus = 'draft' | 'enabled' | 'disabled';

export type UserRole = '管理员' | '普通用户';

export interface UserRecord {
  id: string;
  businessCode: string;
  email: string;
  phone?: string;
  name: string;
  roles: UserRole[];
  status: UserStatus;
  remark?: string;
  updatedAt: string;
}

export interface CurrentUser {
  name: string;
  roles: UserRole[];
}
