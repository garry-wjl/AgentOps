import { request } from '@/utils/request';

export interface ModelDTO {
  num: string;
  spaceCode: string;
  name: string;
  modelId: string;
  baseUrl: string;
  apiKey: string;
  remark?: string;
  status: string;
  createTime: string;
  updateTime: string;
}

export function createModel(spaceCode: string, name: string, modelId: string, baseUrl: string, apiKey: string, remark?: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/create', method: 'POST', data: { spaceCode, name, modelId, baseUrl, apiKey, remark } });
}

export function updateModel(num: string, data: { name?: string; modelId?: string; baseUrl?: string; apiKey?: string; remark?: string }): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/update', method: 'POST', data: { num, ...data } });
}

export function enableModel(num: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/enable', method: 'POST', data: { num } });
}

export function disableModel(num: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/disable', method: 'POST', data: { num } });
}

export function deleteModel(num: string): Promise<boolean> {
  return request<boolean>({ url: '/models/delete', method: 'POST', data: { num } });
}

export function getModel(num: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/get', method: 'GET', params: { num } });
}

export function listEnabledModels(spaceCode: string): Promise<ModelDTO[]> {
  return request<ModelDTO[]>({ url: '/models/list-enabled', method: 'GET', params: { spaceCode } });
}