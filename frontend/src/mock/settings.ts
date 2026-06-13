/**
 * 系统设置 Mock 数据 —— 对应《系统设置 PRD V1.2》。
 * 含平台基础信息 / 邮件 / 空间策略 / 沙箱接入 / 审计日志 五分组。
 */

export interface PlatformBasicSettings {
  platformName: string;
  logoUrl?: string;
  icpNumber?: string;
  supportEmail?: string;
  supportPhone?: string;
}

export interface MailSettings {
  enabled: boolean;
  host: string;
  port: number;
  encryption: 'NONE' | 'SSL' | 'STARTTLS';
  account: string;
  passwordConfigured: boolean;
  fromName: string;
}

export interface SpacePolicySettings {
  maxSpacesPerUser: number;
  spaceNameMinLen: number;
  spaceNameMaxLen: number;
  charset: 'EN_NUM' | 'CN_EN_NUM' | 'CN_EN_NUM_SYM';
}

export interface SandboxAccessSettings {
  openSandboxEnabled: boolean;
  openSandboxEndpoint: string;
  openSandboxTokenConfigured: boolean;
  aliyunEnabled: boolean;
  aliyunEndpoint: string;
  aliyunRegion: string;
  aliyunAkConfigured: boolean;
  probeIntervalSec: number;
  probeTimeoutSec: number;
  firstStartTimeoutSec: number;
}

export interface AuditLogItem {
  id: string;
  occurredAt: string;
  operator: string;
  resourceType: '用户' | '空间' | '沙箱' | '系统设置' | 'Prompt' | 'Skill' | '工具' | '模型';
  resourceNum: string;
  action: string;
  ip: string;
  ua: string;
  changeSummary: string;
}

export const mockPlatformBasic: PlatformBasicSettings = {
  platformName: 'AgentOps',
  logoUrl: '',
  icpNumber: '京 ICP 备 12345678 号',
  supportEmail: 'support@agentops.example',
  supportPhone: '400-000-0000',
};

export const mockMailSettings: MailSettings = {
  enabled: true,
  host: 'smtp.example.com',
  port: 465,
  encryption: 'SSL',
  account: 'no-reply@example.com',
  passwordConfigured: true,
  fromName: 'AgentOps',
};

export const mockSpacePolicy: SpacePolicySettings = {
  maxSpacesPerUser: 10,
  spaceNameMinLen: 1,
  spaceNameMaxLen: 30,
  charset: 'CN_EN_NUM_SYM',
};

export const mockSandboxAccess: SandboxAccessSettings = {
  openSandboxEnabled: true,
  openSandboxEndpoint: 'https://opensandbox.internal:8080',
  openSandboxTokenConfigured: true,
  aliyunEnabled: false,
  aliyunEndpoint: 'https://sandbox.aliyuncs.com',
  aliyunRegion: 'cn-hangzhou',
  aliyunAkConfigured: false,
  probeIntervalSec: 60,
  probeTimeoutSec: 5,
  firstStartTimeoutSec: 120,
};

export const mockAuditLogs: AuditLogItem[] = [
  {
    id: 'log-001',
    occurredAt: '2026-06-13 15:42:18',
    operator: '张三 (US001)',
    resourceType: '空间',
    resourceNum: 'SP202606131426301234567',
    action: '新增',
    ip: '10.0.12.34',
    ua: 'Chrome 130 / macOS',
    changeSummary: '名称: (空) → 家庭客服 Agent',
  },
  {
    id: 'log-002',
    occurredAt: '2026-06-13 15:30:11',
    operator: '系统',
    resourceType: '沙箱',
    resourceNum: 'SB202606131426301234567',
    action: '探活成功 → 在线',
    ip: '-',
    ua: '-',
    changeSummary: '状态: 离线 → 在线',
  },
  {
    id: 'log-003',
    occurredAt: '2026-06-13 14:18:02',
    operator: '李四 (US002)',
    resourceType: '用户',
    resourceNum: 'US007',
    action: '启用',
    ip: '10.0.12.35',
    ua: 'Chrome 130 / Windows',
    changeSummary: '状态: 禁用 → 启用',
  },
  {
    id: 'log-004',
    occurredAt: '2026-06-13 11:02:30',
    operator: '张三 (US001)',
    resourceType: '系统设置',
    resourceNum: '-',
    action: '修改邮箱配置',
    ip: '10.0.12.34',
    ua: 'Chrome 130 / macOS',
    changeSummary: 'host: smtp.old → smtp.example.com',
  },
  {
    id: 'log-005',
    occurredAt: '2026-06-13 10:00:15',
    operator: '王五 (US003)',
    resourceType: 'Prompt',
    resourceNum: 'PR202606131426301234567',
    action: '提交',
    ip: '10.0.12.36',
    ua: 'Chrome 130 / Windows',
    changeSummary: '状态: 草稿 → 启用',
  },
];
