import { request } from '@/utils/request';

export interface SkillDTO {
  num: string;
  spaceCode: string;
  name: string;
  description: string;
  currentVersionNo?: string;
  status: string;
  tags?: string[];
  remark?: string;
  createTime: string;
  updateTime: string;
}

export interface SkillVersionDTO {
  num: string;
  skillCode: string;
  versionNo: string;
  skillMdContent: string;
  status: string;
  publishTime?: string;
  withdrawTime?: string;
  createTime: string;
  updateTime: string;
}

export interface SkillResourceFileDTO {
  num: string;
  skillVersionCode: string;
  path: string;
  type: string;
  content?: string;
  sizeBytes?: number;
  createTime: string;
  updateTime: string;
}

export function createSkill(spaceCode: string, name: string, description: string, tags?: string[], remark?: string, initialSkillMd?: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/create', method: 'POST', data: { spaceCode, name, description, tags, remark, initialSkillMd } });
}

export function updateSkillBasic(num: string, data: { description?: string; tags?: string[]; remark?: string }): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/update-basic', method: 'POST', data: { num, ...data } });
}

export function enableSkill(num: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/enable', method: 'POST', data: { num } });
}

export function withdrawSkill(num: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/withdraw', method: 'POST', data: { num } });
}

export function deleteSkill(num: string): Promise<boolean> {
  return request<boolean>({ url: '/skills/delete', method: 'POST', data: { num } });
}

export function getSkill(num: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/get', method: 'GET', params: { num } });
}

export function listEffectiveSkills(spaceCode: string): Promise<SkillDTO[]> {
  return request<SkillDTO[]>({ url: '/skills/list-effective', method: 'GET', params: { spaceCode } });
}

export function listSkillVersions(skillCode: string): Promise<SkillVersionDTO[]> {
  return request<SkillVersionDTO[]>({ url: '/skills/versions', method: 'GET', params: { skillCode } });
}

export function getEffectiveVersion(skillCode: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skills/version-effective', method: 'GET', params: { skillCode } });
}

export function deriveDraftVersion(skillCode: string, sourceVersionCode: string, newVersionNo: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skill-versions/derive-draft', method: 'POST', data: { skillCode, sourceVersionCode, newVersionNo } });
}

export function editSkillVersionContent(num: string, skillMdContent: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skill-versions/edit-content', method: 'POST', data: { num, skillMdContent } });
}

export function publishSkillVersion(num: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skill-versions/publish', method: 'POST', data: { num } });
}

export function withdrawSkillVersion(num: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skill-versions/withdraw', method: 'POST', data: { num } });
}

export function listResourceFiles(versionCode: string): Promise<SkillResourceFileDTO[]> {
  return request<SkillResourceFileDTO[]>({ url: '/skills/resources', method: 'GET', params: { versionCode } });
}

export function createResourceFile(versionCode: string, path: string, type: string, content?: string): Promise<SkillResourceFileDTO> {
  return request<SkillResourceFileDTO>({ url: '/skill-resources/create', method: 'POST', data: { versionCode, path, type, content } });
}

export function updateResourceContent(num: string, content: string): Promise<SkillResourceFileDTO> {
  return request<SkillResourceFileDTO>({ url: '/skill-resources/update-content', method: 'POST', data: { num, content } });
}

export function renameResource(num: string, newPath: string): Promise<SkillResourceFileDTO> {
  return request<SkillResourceFileDTO>({ url: '/skill-resources/rename', method: 'POST', data: { num, newPath } });
}

export function deleteResource(num: string): Promise<boolean> {
  return request<boolean>({ url: '/skill-resources/delete', method: 'POST', data: { num } });
}