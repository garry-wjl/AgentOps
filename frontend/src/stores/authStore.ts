import { create } from 'zustand';
import { getCurrentUser, login as loginApi, logout as logoutApi } from '@/api/auth';
import { clearAccessToken, getAccessToken, setAccessToken } from '@/utils/request';
import type { CurrentUserVO } from '@/types/api';

interface AuthState {
  /** 当前登录用户。 */
  currentUser: CurrentUserVO | null;
  /** 当前 access token。 */
  token: string | null;
  /** 初始化加载中。 */
  loading: boolean;
  /** 是否已经从后端拉取过当前用户。 */
  initialized: boolean;
  login: (account: string, password: string) => Promise<CurrentUserVO>;
  logout: () => Promise<void>;
  fetchCurrent: () => Promise<CurrentUserVO | null>;
  reset: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  currentUser: null,
  token: getAccessToken(),
  loading: false,
  initialized: false,

  async login(account, password) {
    const result = await loginApi(account, password);
    setAccessToken(result.accessToken);
    set({ token: result.accessToken, currentUser: result.user, initialized: true });
    return result.user;
  },

  async logout() {
    const token = get().token;
    try {
      if (token) {
        await logoutApi(token);
      }
    } finally {
      clearAccessToken();
      set({ token: null, currentUser: null, initialized: true });
    }
  },

  async fetchCurrent() {
    if (!get().token) {
      set({ initialized: true });
      return null;
    }
    set({ loading: true });
    try {
      const user = await getCurrentUser();
      set({ currentUser: user, initialized: true });
      return user;
    } catch (e) {
      clearAccessToken();
      set({ token: null, currentUser: null, initialized: true });
      return null;
    } finally {
      set({ loading: false });
    }
  },

  reset() {
    clearAccessToken();
    set({ token: null, currentUser: null, initialized: true });
  },
}));

/**
 * 判断给定角色集合中是否包含管理员角色。
 */
export function hasAdminRole(currentUser: CurrentUserVO | null): boolean {
  if (!currentUser) return false;
  return currentUser.roles.some((role) => role.code === 'ADMIN');
}
