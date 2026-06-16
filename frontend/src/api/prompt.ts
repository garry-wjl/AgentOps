import { request } from '@/utils/request';
import type { PageWrapper } from '@/api/space';

/**
 * Prompt 生命周期状态。
 */
export type PromptStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

/**
 * Prompt DTO（详情/写接口返回）。
 */
export interface PromptDTO {
  num: string;
  spaceCode: string;
  name: string;
  key: string;
  content: string;
  variables?: string[];
  remark?: string;
  status: PromptStatus;
  createTime: string;
  updateTime: string;
}

/**
 * Prompt 列表视图对象（分页接口返回）。
 */
export interface PromptVO {
  num: string;
  name: string;
  key: string;
  contentPreview: string;
  variables?: string[];
  remark?: string;
  status: PromptStatus;
  updateTime: string;
}

/**
 * 创建 Prompt（自动落为草稿态）。
 */
export function createPrompt(
  spaceCode: string,
  data: { name: string; key: string; content: string; remark?: string },
): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/create', method: 'POST', data: { spaceCode, ...data } });
}

/**
 * 更新 Prompt（newKey 仅草稿态可改）。
 */
export function updatePrompt(
  num: string,
  data: { name?: string; content?: string; newKey?: string; remark?: string },
): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/update', method: 'POST', data: { num, ...data } });
}

/**
 * 提交 Prompt：草稿 → 启用。
 */
export function submitPrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/submit', method: 'POST', data: { num } });
}

/**
 * 启用 Prompt。
 */
export function enablePrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/enable', method: 'POST', data: { num } });
}

/**
 * 禁用 Prompt。
 */
export function disablePrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/disable', method: 'POST', data: { num } });
}

/**
 * 删除 Prompt。
 */
export function deletePrompt(num: string): Promise<boolean> {
  return request<boolean>({ url: '/prompts/delete', method: 'POST', data: { num } });
}

/**
 * 查询 Prompt 详情。
 */
export function getPrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/get', method: 'GET', params: { num } });
}

/**
 * 列出空间内启用状态的 Prompt。
 */
export function listEnabledPrompts(spaceCode: string): Promise<PromptDTO[]> {
  return request<PromptDTO[]>({ url: '/prompts/list-enabled', method: 'GET', params: { spaceCode } });
}

/**
 * 通过 (spaceCode, key) 查询启用态 Prompt。
 */
export function getEnabledPromptByKey(spaceCode: string, key: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/get-by-key', method: 'GET', params: { spaceCode, key } });
}

/**
 * Prompt 分页查询。
 */
export function pagePrompts(
  spaceCode: string,
  params: { keyword?: string; status?: PromptStatus | ''; pageNo?: number; pageSize?: number } = {},
): Promise<PageWrapper<PromptVO>> {
  return request<PageWrapper<PromptVO>>({
    url: '/prompts/page',
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
