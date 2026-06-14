import { request } from '@/utils/request';

export interface AgentDTO {
  num: string;
  spaceCode: string;
  name: string;
  displayName?: string;
  description?: string;
  currentVersionNo?: string;
  status: string;
  tags?: string[];
  remark?: string;
  createTime: string;
  updateTime: string;
}

export interface AssemblySnapshotDTO {
  modelCode?: string;
  modelParamsJson?: string;
  systemPromptContent?: string;
  systemPromptSourceCode?: string;
  userPromptContent?: string;
  userPromptSourceCode?: string;
  skillCodes?: string[];
  toolCodes?: string[];
  sandboxCode?: string;
  shortMemoryTurns?: number;
}

export interface AgentVersionDTO {
  num: string;
  agentCode: string;
  versionNo: string;
  snapshot: AssemblySnapshotDTO;
  status: string;
  onlineTime?: string;
  offlineTime?: string;
  createTime: string;
  updateTime: string;
}

export interface PrePublishCheckVO {
  passed: boolean;
  errors: { field: string; code: string; message: string }[];
  warnings: { field: string; code: string; message: string }[];
}

export function createAgent(spaceCode: string, name: string, displayName?: string, description?: string, tags?: string[], remark?: string, versionNo?: string, initialAssembly?: AssemblySnapshotDTO): Promise<AgentDTO> {
  return request<AgentDTO>({ url: '/agents/create', method: 'POST', data: { spaceCode, name, displayName, description, tags, remark, versionNo, initialAssembly } });
}

export function updateAgentBasic(num: string, data: { displayName?: string; description?: string; tags?: string[]; remark?: string }): Promise<AgentDTO> {
  return request<AgentDTO>({ url: '/agents/update-basic', method: 'POST', data: { num, ...data } });
}

export function enableAgent(num: string): Promise<AgentDTO> {
  return request<AgentDTO>({ url: '/agents/enable', method: 'POST', data: { num } });
}

export function withdrawAgent(num: string): Promise<AgentDTO> {
  return request<AgentDTO>({ url: '/agents/withdraw', method: 'POST', data: { num } });
}

export function deleteAgent(num: string): Promise<boolean> {
  return request<boolean>({ url: '/agents/delete', method: 'POST', data: { num } });
}

export function getAgent(num: string): Promise<AgentDTO> {
  return request<AgentDTO>({ url: '/agents/get', method: 'GET', params: { num } });
}

export function getOnlineAgentByName(spaceCode: string, name: string): Promise<AgentVersionDTO> {
  return request<AgentVersionDTO>({ url: '/agents/get-online-by-name', method: 'GET', params: { spaceCode, name } });
}

export function listAgentVersions(agentCode: string): Promise<AgentVersionDTO[]> {
  return request<AgentVersionDTO[]>({ url: '/agents/versions', method: 'GET', params: { agentCode } });
}

export function deriveAgentVersion(agentCode: string, sourceVersionCode: string, newVersionNo: string): Promise<AgentVersionDTO> {
  return request<AgentVersionDTO>({ url: '/agent-versions/derive-draft', method: 'POST', data: { agentCode, sourceVersionCode, newVersionNo } });
}

export function editAgentAssembly(num: string, snapshot: AssemblySnapshotDTO): Promise<AgentVersionDTO> {
  return request<AgentVersionDTO>({ url: '/agent-versions/edit-assembly', method: 'POST', data: { num, snapshot } });
}

export function prePublishCheck(num: string): Promise<PrePublishCheckVO> {
  return request<PrePublishCheckVO>({ url: '/agent-versions/pre-publish-check', method: 'GET', params: { num } });
}

export function publishAgentVersion(num: string): Promise<AgentVersionDTO> {
  return request<AgentVersionDTO>({ url: '/agent-versions/publish', method: 'POST', data: { num } });
}

export function offlineAgentVersion(num: string): Promise<AgentVersionDTO> {
  return request<AgentVersionDTO>({ url: '/agent-versions/offline', method: 'POST', data: { num } });
}

export function deleteAgentVersion(num: string): Promise<boolean> {
  return request<boolean>({ url: '/agent-versions/delete-draft', method: 'POST', data: { num } });
}