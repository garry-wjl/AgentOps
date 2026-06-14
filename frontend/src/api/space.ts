import { request } from '@/utils/request';

export interface SpaceCardVO {
  num: string;
  name: string;
  description?: string;
  iconUrl?: string;
  ownerUserCode: string;
  currentUserRole: string;
  adminCount: number;
  memberCount: number;
  createTime: string;
  updateTime: string;
}

export interface SpaceDTO {
  num: string;
  name: string;
  description?: string;
  iconUrl?: string;
  ownerUserCode: string;
  status: string;
  adminUserCodes: string[];
  memberUserCodes: string[];
  createTime: string;
  updateTime: string;
}

export interface SpaceMemberVO {
  userCode: string;
  userName: string;
  email?: string;
  phone?: string;
  roleType: string;
  owner: boolean;
}

export interface PageWrapper<T> {
  total: number;
  pageNo: number;
  pageSize: number;
  records: T[];
}

export function createSpace(name: string, description?: string, iconUrl?: string): Promise<SpaceDTO> {
  return request<SpaceDTO>({ url: '/spaces/create', method: 'POST', data: { name, description, iconUrl } });
}

export function getSpace(code: string): Promise<SpaceDTO> {
  return request<SpaceDTO>({ url: '/spaces/get', method: 'GET', params: { code } });
}

export function updateSpaceBasic(spaceCode: string, data: { name?: string; description?: string }): Promise<SpaceDTO> {
  return request<SpaceDTO>({ url: '/spaces/update-basic', method: 'POST', data: { spaceCode, ...data } });
}

export function pageMine(keyword?: string, pageNo = 1, pageSize = 12): Promise<PageWrapper<SpaceCardVO>> {
  return request<PageWrapper<SpaceCardVO>>({ url: '/spaces/page-mine', method: 'GET', params: { keyword, pageNo, pageSize } });
}

export function pageMembers(spaceCode: string, keyword?: string, pageNo = 1, pageSize = 20): Promise<PageWrapper<SpaceMemberVO>> {
  return request<PageWrapper<SpaceMemberVO>>({ url: '/spaces/page-members', method: 'GET', params: { spaceCode, keyword, pageNo, pageSize } });
}

export function addMember(spaceCode: string, userCode: string, roleType: string): Promise<SpaceDTO> {
  return request<SpaceDTO>({ url: '/spaces/add-member', method: 'POST', data: { spaceCode, userCode, roleType } });
}

export function removeMember(spaceCode: string, userCode: string): Promise<SpaceDTO> {
  return request<SpaceDTO>({ url: '/spaces/remove-member', method: 'POST', data: { spaceCode, userCode } });
}

export function deleteSpace(spaceCode: string, confirmName: string): Promise<boolean> {
  return request<boolean>({ url: '/spaces/delete', method: 'POST', data: { spaceCode, confirmName } });
}