import { request } from '@/utils/request';

export interface SandboxDTO {
  num: string;
  spaceCode: string;
  name: string;
  image: string;
  baseUrlOverride?: string;
  remark?: string;
  status: string;
  lastStatusReason?: string;
  lastHeartbeatTime?: string;
  createTime: string;
  updateTime: string;
}

export function createSandbox(spaceCode: string, name: string, image: string, baseUrlOverride?: string, remark?: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/create', method: 'POST', data: { spaceCode, name, image, baseUrlOverride, remark } });
}

export function updateSandbox(num: string, data: { name?: string; image?: string; baseUrlOverride?: string; remark?: string }): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/update', method: 'POST', data: { num, ...data } });
}

export function submitSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/submit', method: 'POST', data: { num } });
}

export function disableSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/disable', method: 'POST', data: { num } });
}

export function reEnableSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/re-enable', method: 'POST', data: { num } });
}

export function deleteSandbox(num: string): Promise<boolean> {
  return request<boolean>({ url: '/sandboxes/delete', method: 'POST', data: { num } });
}

export function getSandbox(num: string): Promise<SandboxDTO> {
  return request<SandboxDTO>({ url: '/sandboxes/get', method: 'GET', params: { num } });
}

export function listAvailableSandboxes(spaceCode: string): Promise<SandboxDTO[]> {
  return request<SandboxDTO[]>({ url: '/sandboxes/list-available', method: 'GET', params: { spaceCode } });
}