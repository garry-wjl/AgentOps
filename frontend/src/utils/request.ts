import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { message } from 'antd';
import type { ApiResult } from '@/types/api';

const TOKEN_STORAGE_KEY = 'agentops.accessToken';

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setAccessToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearAccessToken(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

http.interceptors.response.use(
  (response: AxiosResponse<ApiResult<unknown>>) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      clearAccessToken();
      if (location.pathname !== '/login') {
        location.replace('/login');
      }
    }
    return Promise.reject(error);
  },
);

/**
 * 统一调用后端接口并解包 Result<T>。
 * 后端业务异常会以 code !== '0' 的形式返回，这里抛出错误供调用方处理。
 */
export async function request<T>(config: Parameters<AxiosInstance['request']>[0]): Promise<T> {
  const response = await http.request<ApiResult<T>>(config);
  const body = response.data;
  if (!body || body.code !== '0') {
    const errMsg = body?.message || '请求失败';
    const error = new Error(errMsg) as Error & { code?: string };
    error.code = body?.code;
    throw error;
  }
  return body.data;
}

/**
 * 直接弹出错误信息的帮助函数。
 */
export function notifyError(error: unknown, fallback = '请求失败'): void {
  if (error instanceof Error) {
    message.error(error.message || fallback);
    return;
  }
  message.error(fallback);
}

export default http;
