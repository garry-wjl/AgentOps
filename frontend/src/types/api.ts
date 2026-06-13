/**
 * 后端 Result<T> 统一返回结构。
 */
export interface ApiResult<T> {
  code: string;
  message: string;
  data: T;
  traceId?: string;
}

/**
 * 用户状态枚举，与后端 UserStatus 保持一致。
 */
export type UserStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

/**
 * 角色编码，与后端 UserRole 保持一致。
 */
export type UserRoleCode = 'ADMIN' | 'MEMBER';

/**
 * 用户角色 DTO。
 */
export interface UserRoleDTO {
  code: string;
  label: string;
}

/**
 * 用户视图对象，对应后端 UserVO。
 */
export interface UserVO {
  id: number;
  num: string;
  email: string;
  phone?: string;
  name: string;
  roles: UserRoleDTO[];
  status: UserStatus;
  remark?: string;
}

/**
 * 用户分页视图，对应后端 UserPageVO。
 */
export interface UserPageVO {
  total: number;
  pageNo: number;
  pageSize: number;
  records: UserVO[];
}

/**
 * 当前用户视图对象。
 */
export interface CurrentUserVO {
  id: number;
  num: string;
  name: string;
  email?: string;
  phone?: string;
  roles: UserRoleDTO[];
  menus: string[];
}

/**
 * 登录结果视图对象。
 */
export interface LoginResultVO {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: CurrentUserVO;
}
