import { request } from '@/utils/request';
import type { CurrentUserVO, LoginResultVO } from '@/types/api';

/**
 * 登录。
 */
export function login(account: string, password: string): Promise<LoginResultVO> {
  return request<LoginResultVO>({
    url: '/auth/login',
    method: 'POST',
    data: { account, password },
  });
}

/**
 * 退出。
 */
export function logout(token: string): Promise<boolean> {
  return request<boolean>({
    url: '/auth/logout',
    method: 'POST',
    data: { token },
  });
}

/**
 * 当前登录用户。
 */
export function getCurrentUser(): Promise<CurrentUserVO> {
  return request<CurrentUserVO>({
    url: '/auth/current',
    method: 'GET',
  });
}
