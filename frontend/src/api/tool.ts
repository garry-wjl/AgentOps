import { request } from '@/utils/request';

export interface ToolDTO {
  num: string;
  spaceCode: string;
  name: string;
  type: string;
  subType: string;
  description?: string;
  tags?: string[];
  configJson: string;
  status: string;
  remark?: string;
  createTime: string;
  updateTime: string;
}

export interface TestResultDTO {
  success: boolean;
  durationMs?: number;
  request?: Record<string, unknown>;
  response?: Record<string, unknown>;
  errorMessage?: string;
}

export function createTool(spaceCode: string, name: string, type: string, subType: string, configJson: string, description?: string, tags?: string[], remark?: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/create', method: 'POST', data: { spaceCode, name, type, subType, configJson, description, tags, remark } });
}

export function updateTool(num: string, data: { name?: string; configJson?: string; description?: string; tags?: string[]; remark?: string }): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/update', method: 'POST', data: { num, ...data } });
}

export function publishTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/publish', method: 'POST', data: { num } });
}

export function withdrawTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/withdraw', method: 'POST', data: { num } });
}

export function republishTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/republish', method: 'POST', data: { num } });
}

export function deleteTool(num: string): Promise<boolean> {
  return request<boolean>({ url: '/tools/delete', method: 'POST', data: { num } });
}

export function getTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/get', method: 'GET', params: { num } });
}

export function listEffectiveTools(spaceCode: string): Promise<ToolDTO[]> {
  return request<ToolDTO[]>({ url: '/tools/list-effective', method: 'GET', params: { spaceCode } });
}

export function testTool(num: string, testInput?: Record<string, unknown>): Promise<TestResultDTO> {
  return request<TestResultDTO>({ url: '/tools/test', method: 'POST', data: { num, testInput } });
}