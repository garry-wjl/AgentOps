import { request } from '@/utils/request';

export interface PlatformBasicDTO {
  platformName: string;
  logoUrl?: string;
  encryptionKey?: string;
}

export interface SmtpConfigDTO {
  host?: string;
  port?: number;
  username?: string;
  password?: string;
  from?: string;
  ssl?: boolean;
}

export interface SpacePolicyDTO {
  quotaPerUser?: number;
  namingRegex?: string;
}

export interface SandboxDefaultDTO {
  baseUrl?: string;
  heartbeatIntervalSec?: number;
}

export interface AuditLogDTO {
  num: string;
  module: string;
  action: string;
  operatorCode: string;
  targetNum?: string;
  detailJson?: string;
  createTime: string;
}

export function getPlatformBasic(): Promise<PlatformBasicDTO> {
  return request<PlatformBasicDTO>({ url: '/system-settings/platform-basic', method: 'GET' });
}

export function updatePlatformBasic(data: { platformName?: string; logoUrl?: string; encryptionKey?: string }): Promise<PlatformBasicDTO> {
  return request<PlatformBasicDTO>({ url: '/system-settings/platform-basic/update', method: 'POST', data });
}

export function getSmtpConfig(): Promise<SmtpConfigDTO> {
  return request<SmtpConfigDTO>({ url: '/system-settings/smtp', method: 'GET' });
}

export function updateSmtpConfig(data: SmtpConfigDTO): Promise<SmtpConfigDTO> {
  return request<SmtpConfigDTO>({ url: '/system-settings/smtp/update', method: 'POST', data });
}

export function sendTestMail(to: string): Promise<boolean> {
  return request<boolean>({ url: '/system-settings/smtp/send-test-mail', method: 'POST', data: { to } });
}

export function getSpacePolicy(): Promise<SpacePolicyDTO> {
  return request<SpacePolicyDTO>({ url: '/system-settings/space-policy', method: 'GET' });
}

export function updateSpacePolicy(data: SpacePolicyDTO): Promise<SpacePolicyDTO> {
  return request<SpacePolicyDTO>({ url: '/system-settings/space-policy/update', method: 'POST', data });
}

export function getSandboxDefault(): Promise<SandboxDefaultDTO> {
  return request<SandboxDefaultDTO>({ url: '/system-settings/sandbox-default', method: 'GET' });
}

export function updateSandboxDefault(data: SandboxDefaultDTO): Promise<SandboxDefaultDTO> {
  return request<SandboxDefaultDTO>({ url: '/system-settings/sandbox-default/update', method: 'POST', data });
}

export function pageAuditLogs(module?: string, action?: string, pageNo = 1, pageSize = 20): Promise<{ total: number; pageNo: number; pageSize: number; records: AuditLogDTO[] }> {
  return request({ url: '/audit-logs/page', method: 'GET', params: { module, action, pageNo, pageSize } });
}