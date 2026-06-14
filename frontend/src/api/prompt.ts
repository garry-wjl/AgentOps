import { request } from '@/utils/request';

export interface PromptDTO {
  num: string;
  spaceCode: string;
  name: string;
  key: string;
  content: string;
  variables?: string[];
  remark?: string;
  status: string;
  createTime: string;
  updateTime: string;
}

export function createPrompt(spaceCode: string, name: string, key: string, content: string, remark?: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/create', method: 'POST', data: { spaceCode, name, key, content, remark } });
}

export function updatePrompt(num: string, data: { name?: string; content?: string; newKey?: string; remark?: string }): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/update', method: 'POST', data: { num, ...data } });
}

export function submitPrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/submit', method: 'POST', data: { num } });
}

export function enablePrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/enable', method: 'POST', data: { num } });
}

export function disablePrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/disable', method: 'POST', data: { num } });
}

export function deletePrompt(num: string): Promise<boolean> {
  return request<boolean>({ url: '/prompts/delete', method: 'POST', data: { num } });
}

export function getPrompt(num: string): Promise<PromptDTO> {
  return request<PromptDTO>({ url: '/prompts/get', method: 'GET', params: { num } });
}

export function listEnabledPrompts(spaceCode: string): Promise<PromptDTO[]> {
  return request<PromptDTO[]>({ url: '/prompts/list-enabled', method: 'GET', params: { spaceCode } });
}