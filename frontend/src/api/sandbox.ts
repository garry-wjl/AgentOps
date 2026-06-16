import { request } from '@/utils/request';
import type { PageWrapper } from '@/api/space';

/**
 * 沙箱生命周期状态。
 */
export type SandboxStatus = 'DRAFT' | 'INITIALIZING' | 'ONLINE' | 'OFFLINE' | 'DISABLED';

/**
 * 沙箱详情 DTO。
 */
export interface SandboxDTO {
  num: string;
  spaceCode: string;
  name: string;
  image: string;
  baseUrlOverride?: string;
  remark?: string;
  status: SandboxStatus;
  lastStatusReason?: string;
  lastHeartbeatTime?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 沙箱列表视图对象。
 */
export interface SandboxVO {
  num: string;
  name: string;
  image: string;
  baseUrlOverride?: string;
  remark?: string;
  status: SandboxStatus;
  lastStatusReason?: string;
  lastHeartbeatTime?: string;
  updateTime: string;
}

/**
 * 创建沙箱。
 */
export function createSandbox(
  spaceCode: string,
  data: { name: string; image: string; baseUrlOverride?: string; remark?: string },
): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/create', method: 'POST', data: { spaceCode, ...data } });
}

/**
 * 更新沙箱。
 */
export function updateSandbox(
  num: string,
  data: { name?: string; image?: string; baseUrlOverride?: string; remark?: string },
): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/update', method: 'POST', data: { num, ...data } });
}

/**
 * 提交沙箱：DRAFT → INITIALIZING。
 */
export function submitSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/submit', method: 'POST', data: { num } });
}

/**
 * 禁用沙箱。
 */
export function disableSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/disable', method: 'POST', data: { num } });
}

/**
 * 重新启用：DISABLED → INITIALIZING。
 */
export function reEnableSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/re-enable', method: 'POST', data: { num } });
}

/**
 * 删除沙箱（仅草稿）。
 */
export function deleteSandbox(num: string): Promise<boolean> {
  return request<boolean>({ url: '/sandboxes/delete', method: 'POST', data: { num } });
}

/**
 * 查询沙箱详情。
 */
export function getSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/get', method: 'GET', params: { num } });
}

/**
 * 列出空间内可用沙箱。
 */
export function listAvailableSandboxes(spaceCode: string): Promise<SandboxDTO[]> {
  return request<SandboxDTO[]>({ url: '/sandboxes/list-available', method: 'GET', params: { spaceCode } });
}

/**
 * 沙箱分页查询。
 */
export function pageSandboxes(
  spaceCode: string,
  params: { keyword?: string; status?: SandboxStatus | ''; pageNo?: number; pageSize?: number } = {},
): Promise<PageWrapper<SandboxVO>> {
  return request<PageWrapper<SandboxVO>>({
    url: '/sandboxes/page',
    method: 'GET',
    params: {
      spaceCode,
      keyword: params.keyword,
      status: params.status,
      pageNo: params.pageNo ?? 1,
      pageSize: params.pageSize ?? 10,
    },
  });
}
