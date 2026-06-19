import { request } from '@/utils/request';
import type { PageWrapper } from '@/api/space';

/**
 * 模型生命周期状态。
 */
export type ModelStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

/**
 * 模型 DTO（详情/写接口返回）。
 */
export interface ModelDTO {
  num: string;
  spaceCode: string;
  name: string;
  modelId: string;
  baseUrl: string;
  /** 脱敏后的 API Key。 */
  apiKey: string;
  remark?: string;
  status: ModelStatus;
  createTime: string;
  updateTime: string;
}

/**
 * 模型列表视图对象（分页接口返回）。
 */
export interface ModelVO {
  num: string;
  name: string;
  modelId: string;
  baseUrl: string;
  apiKey: string;
  remark?: string;
  status: ModelStatus;
  updateTime: string;
}

/**
 * 创建模型。
 */
export function createModel(
  spaceCode: string,
  data: { name: string; modelId: string; baseUrl: string; apiKey: string; remark?: string },
): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/create', method: 'POST', data: { spaceCode, ...data } });
}

/**
 * 更新模型。
 */
export function updateModel(
  num: string,
  data: { name?: string; modelId?: string; baseUrl?: string; apiKey?: string; remark?: string },
): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/update', method: 'POST', data: { num, ...data } });
}

/**
 * 启用模型。
 */
export function enableModel(num: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/enable', method: 'POST', data: { num } });
}

/**
 * 禁用模型。
 */
export function disableModel(num: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/disable', method: 'POST', data: { num } });
}

/**
 * 删除模型。
 */
export function deleteModel(num: string): Promise<boolean> {
  return request<boolean>({ url: '/models/delete', method: 'POST', data: { num } });
}

/**
 * 查询模型详情。
 */
export function getModel(num: string): Promise<ModelDTO> {
  return request<ModelDTO>({ url: '/models/get', method: 'GET', params: { num } });
}

/**
 * 列出空间内启用状态的模型。
 */
export function listEnabledModels(spaceCode: string): Promise<ModelDTO[]> {
  return request<ModelDTO[]>({ url: '/models/list-enabled', method: 'GET', params: { spaceCode } });
}

/**
 * 模型分页查询。
 */
export function pageModels(
  spaceCode: string,
  params: { keyword?: string; status?: ModelStatus | ''; pageNo?: number; pageSize?: number } = {},
): Promise<PageWrapper<ModelVO>> {
  return request<PageWrapper<ModelVO>>({
    url: '/models/page',
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
