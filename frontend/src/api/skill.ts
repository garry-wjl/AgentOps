import { request } from '@/utils/request';
import type { PageWrapper } from '@/api/space';

/**
 * Skill 主体生命周期状态。
 */
export type SkillStatus = 'DRAFT' | 'EFFECTIVE' | 'WITHDRAWN';

/**
 * Skill 详情 DTO。
 */
export interface SkillDTO {
  num: string;
  spaceCode: string;
  name: string;
  description: string;
  currentVersionNo?: string;
  status: SkillStatus;
  tags?: string[];
  remark?: string;
  createTime: string;
  updateTime: string;
}

/**
 * Skill 列表视图对象。
 */
export interface SkillVO {
  num: string;
  name: string;
  description: string;
  currentVersionNo?: string;
  status: SkillStatus;
  tags?: string[];
  updateTime: string;
}

/**
 * 创建 Skill。
 * 后端会同步生成 V1 草稿版本，initialSkillMd 可选。
 */
export function createSkill(
  spaceCode: string,
  data: {
    name: string;
    description?: string;
    tags?: string[];
    remark?: string;
    initialSkillMd?: string;
  },
): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/create', method: 'POST', data: { spaceCode, ...data } });
}

/**
 * 更新 Skill 基础信息。
 */
export function updateSkillBasic(
  num: string,
  data: { description?: string; tags?: string[]; remark?: string },
): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/update-basic', method: 'POST', data: { num, ...data } });
}

/**
 * 启用 Skill。
 */
export function enableSkill(num: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/enable', method: 'POST', data: { num } });
}

/**
 * 停用（撤回） Skill。
 */
export function withdrawSkill(num: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/withdraw', method: 'POST', data: { num } });
}

/**
 * 删除 Skill（仅草稿）。
 */
export function deleteSkill(num: string): Promise<boolean> {
  return request<boolean>({ url: '/skills/delete', method: 'POST', data: { num } });
}

/**
 * 查询 Skill 详情。
 */
export function getSkill(num: string): Promise<SkillDTO> {
  return request<SkillDTO>({ url: '/skills/get', method: 'GET', params: { num } });
}

/**
 * 列出空间内可被引用的 Skill（EFFECTIVE）。
 */
export function listEffectiveSkills(spaceCode: string): Promise<SkillDTO[]> {
  return request<SkillDTO[]>({ url: '/skills/list-effective', method: 'GET', params: { spaceCode } });
}

/**
 * 资源文件类型。
 */
export type ResourceFileType = 'FILE' | 'FOLDER';

/**
 * 资源文件 DTO。
 */
export interface SkillResourceFileDTO {
  num: string;
  skillVersionCode: string;
  path: string;
  type: ResourceFileType;
  content?: string;
  sizeBytes?: number;
  createTime: string;
  updateTime: string;
}

/**
 * Skill 版本 DTO。
 */
export interface SkillVersionDTO {
  num: string;
  skillCode: string;
  versionNo: string;
  skillMdContent: string;
  status: 'DRAFT' | 'EFFECTIVE' | 'WITHDRAWN';
  publishTime?: string;
  withdrawTime?: string;
  createTime: string;
  updateTime: string;
}

/**
 * 列出 Skill 的所有版本。
 */
export function listSkillVersions(skillCode: string): Promise<SkillVersionDTO[]> {
  return request<SkillVersionDTO[]>({ url: '/skills/versions', method: 'GET', params: { skillCode } });
}

/**
 * 列出指定版本下的资源文件。
 */
export function listResourceFiles(versionCode: string): Promise<SkillResourceFileDTO[]> {
  return request<SkillResourceFileDTO[]>({ url: '/skills/resources', method: 'GET', params: { versionCode } });
}

/**
 * 创建资源文件。
 */
export function createResourceFile(
  versionCode: string,
  data: { path: string; type: ResourceFileType; content?: string },
): Promise<SkillResourceFileDTO> {
  return request<SkillResourceFileDTO>({
    url: '/skill-resources/create',
    method: 'POST',
    data: { versionCode, ...data },
  });
}

/**
 * 更新资源文件内容。
 */
export function updateResourceContent(num: string, content: string): Promise<SkillResourceFileDTO> {
  return request<SkillResourceFileDTO>({
    url: '/skill-resources/update-content',
    method: 'POST',
    data: { num, content },
  });
}

/**
 * 重命名资源文件。
 */
export function renameResource(num: string, newPath: string): Promise<SkillResourceFileDTO> {
  return request<SkillResourceFileDTO>({
    url: '/skill-resources/rename',
    method: 'POST',
    data: { num, newPath },
  });
}

/**
 * 删除资源文件。
 */
export function deleteResource(num: string): Promise<boolean> {
  return request<boolean>({ url: '/skill-resources/delete', method: 'POST', data: { num } });
}

/**
 * 查询单个版本详情。
 */
export function getSkillVersion(num: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skills/version-get', method: 'GET', params: { num } });
}

/**
 * 查询 Skill 当前生效版本。
 */
export function getEffectiveSkillVersion(skillCode: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skills/version-effective', method: 'GET', params: { skillCode } });
}

/**
 * 从已有版本派生草稿版本。
 */
export function deriveSkillVersion(
  skillCode: string,
  sourceVersionCode: string,
  newVersionNo: string,
): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({
    url: '/skill-versions/derive-draft',
    method: 'POST',
    data: { skillCode, sourceVersionCode, newVersionNo },
  });
}

/**
 * 编辑草稿版本的 SKILL.md 内容。
 */
export function editSkillVersionContent(num: string, skillMdContent: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({
    url: '/skill-versions/edit-content',
    method: 'POST',
    data: { num, skillMdContent },
  });
}

/**
 * 发布草稿版本。
 */
export function publishSkillVersion(num: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skill-versions/publish', method: 'POST', data: { num } });
}

/**
 * 撤回已发布版本。
 */
export function withdrawSkillVersion(num: string): Promise<SkillVersionDTO> {
  return request<SkillVersionDTO>({ url: '/skill-versions/withdraw', method: 'POST', data: { num } });
}

/**
 * 删除草稿版本。
 */
export function deleteSkillVersion(num: string): Promise<boolean> {
  return request<boolean>({ url: '/skill-versions/delete-draft', method: 'POST', data: { num } });
}

/**
 * Skill 分页查询。
 */
export function pageSkills(
  spaceCode: string,
  params: { keyword?: string; status?: SkillStatus | ''; pageNo?: number; pageSize?: number } = {},
): Promise<PageWrapper<SkillVO>> {
  return request<PageWrapper<SkillVO>>({
    url: '/skills/page',
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
