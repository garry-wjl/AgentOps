import { request } from '@/utils/request';
import type { UserPageVO, UserRoleDTO, UserVO } from '@/types/api';

export interface UserPageQuery {
  keyword?: string;
  status?: string;
  role?: string;
  pageNo: number;
  pageSize: number;
}

export interface CreateUserPayload {
  email: string;
  phone?: string;
  name: string;
  roles: string[];
  remark?: string;
}

export interface SaveUserPayload {
  userNum: string;
  email: string;
  phone?: string;
  name: string;
  roles: string[];
  remark?: string;
}

export interface ResetPasswordPayload {
  userNum: string;
  newPassword: string;
  confirmPassword: string;
}

export interface AssignRolesPayload {
  userNum: string;
  roles: string[];
}

/**
 * 用户分页查询。
 */
export function pageUsers(query: UserPageQuery): Promise<UserPageVO> {
  return request<UserPageVO>({
    url: '/users/page',
    method: 'GET',
    params: query,
  });
}

/**
 * 用户详情。
 */
export function detailUser(userNum: string): Promise<UserVO> {
  return request<UserVO>({
    url: '/users/detail',
    method: 'GET',
    params: { userNum },
  });
}

/**
 * 角色选项。
 */
export function roleOptions(): Promise<UserRoleDTO[]> {
  return request<UserRoleDTO[]>({ url: '/users/roles/options', method: 'GET' });
}

/**
 * 用户角色查询。
 */
export function userRoles(userNum: string): Promise<UserRoleDTO[]> {
  return request<UserRoleDTO[]>({
    url: '/users/roles',
    method: 'GET',
    params: { userNum },
  });
}

/**
 * 创建用户草稿。
 */
export function createUser(payload: CreateUserPayload): Promise<UserVO> {
  return request<UserVO>({ url: '/users/create', method: 'POST', data: payload });
}

/**
 * 保存草稿用户资料。
 */
export function saveUser(payload: SaveUserPayload): Promise<UserVO> {
  return request<UserVO>({ url: '/users/save', method: 'POST', data: payload });
}

/**
 * 提交草稿用户。
 */
export function submitUser(userNum: string): Promise<boolean> {
  return request<boolean>({ url: '/users/submit', method: 'POST', data: { userNum } });
}

/**
 * 删除草稿用户。
 */
export function deleteUser(userNum: string): Promise<boolean> {
  return request<boolean>({ url: '/users/delete', method: 'POST', data: { userNum } });
}

/**
 * 启用用户。
 */
export function enableUser(userNum: string): Promise<boolean> {
  return request<boolean>({ url: '/users/enable', method: 'POST', data: { userNum } });
}

/**
 * 禁用用户。
 */
export function disableUser(userNum: string): Promise<boolean> {
  return request<boolean>({ url: '/users/disable', method: 'POST', data: { userNum } });
}

/**
 * 重置用户密码。
 */
export function resetPassword(payload: ResetPasswordPayload): Promise<boolean> {
  return request<boolean>({ url: '/users/reset-password', method: 'POST', data: payload });
}

/**
 * 分配平台角色。
 */
export function assignRoles(payload: AssignRolesPayload): Promise<UserVO> {
  return request<UserVO>({ url: '/users/assign-roles', method: 'POST', data: payload });
}
