import { request } from '@/utils/request';
import type { PageWrapper } from '@/api/space';

/**
 * 工具大类型。
 */
export type ToolType = 'FUNCTION_CALL' | 'MCP';

/**
 * 工具子类型。
 */
export type ToolSubType = 'OPENAPI' | 'ENDPOINT' | 'REMOTE' | 'LOCAL';

/**
 * 工具生命周期状态。
 */
export type ToolStatus = 'DRAFT' | 'EFFECTIVE' | 'WITHDRAWN';

/**
 * 工具详情 DTO。
 */
export interface ToolDTO {
  num: string;
  spaceCode: string;
  name: string;
  type: ToolType;
  subType: ToolSubType;
  description?: string;
  tags?: string[];
  /** 配置 JSON（敏感字段脱敏）。 */
  configJson: string;
  status: ToolStatus;
  remark?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 工具列表视图对象。
 */
export interface ToolVO {
  num: string;
  name: string;
  type: ToolType;
  subType: ToolSubType;
  description?: string;
  tags?: string[];
  status: ToolStatus;
  remark?: string;
  updateTime: string;
}

/**
 * 试运行结果 DTO。
 */
export interface TestResultDTO {
  success: boolean;
  durationMs?: number;
  request?: Record<string, unknown>;
  response?: Record<string, unknown>;
  errorMessage?: string;
}

/**
 * 创建工具。
 */
export function createTool(
  spaceCode: string,
  data: {
    name: string;
    type: ToolType;
    subType: ToolSubType;
    configJson: string;
    description?: string;
    tags?: string[];
    remark?: string;
  },
): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/create', method: 'POST', data: { spaceCode, ...data } });
}

/**
 * 更新工具。
 */
export function updateTool(
  num: string,
  data: {
    name?: string;
    configJson?: string;
    description?: string;
    tags?: string[];
    remark?: string;
  },
): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/update', method: 'POST', data: { num, ...data } });
}

/**
 * 发布工具。
 */
export function publishTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/publish', method: 'POST', data: { num } });
}

/**
 * 撤回工具。
 */
export function withdrawTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/withdraw', method: 'POST', data: { num } });
}

/**
 * 重新发布（已撤回的工具）。
 */
export function republishTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/republish', method: 'POST', data: { num } });
}

/**
 * 删除工具（仅草稿）。
 */
export function deleteTool(num: string): Promise<boolean> {
  return request<boolean>({ url: '/tools/delete', method: 'POST', data: { num } });
}

/**
 * 查询工具详情。
 */
export function getTool(num: string): Promise<ToolDTO> {
  return request<ToolDTO>({ url: '/tools/get', method: 'GET', params: { num } });
}

/**
 * 列出空间内可被引用的工具（EFFECTIVE）。
 */
export function listEffectiveTools(spaceCode: string): Promise<ToolDTO[]> {
  return request<ToolDTO[]>({ url: '/tools/list-effective', method: 'GET', params: { spaceCode } });
}

/**
 * 试运行工具。
 */
export function testTool(num: string, testInput?: Record<string, unknown>): Promise<TestResultDTO> {
  return request<TestResultDTO>({ url: '/tools/test', method: 'POST', data: { num, testInput } });
}

/**
 * 工具分页查询。
 */
export function pageTools(
  spaceCode: string,
  params: { keyword?: string; status?: ToolStatus | ''; pageNo?: number; pageSize?: number } = {},
): Promise<PageWrapper<ToolVO>> {
  return request<PageWrapper<ToolVO>>({
    url: '/tools/page',
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
