/**
 * 沙箱 Mock 数据 —— 对应《沙箱管理 PRD》。
 */

export type SandboxStatus = 'DRAFT' | 'INITIALIZING' | 'ONLINE' | 'OFFLINE' | 'DISABLED';
export type SandboxProvider = 'OPEN_SANDBOX' | 'ALIYUN_SANDBOX';

export interface SandboxEnvVar {
  key: string;
  value: string;
  remark?: string;
}

export interface SandboxItem {
  id: string;
  num: string;
  name: string;
  type: 'CODE';
  provider: SandboxProvider;
  cpu: number;
  memoryMb: number;
  ttlMinutes: number;
  endpointOverride?: string;
  envVars: SandboxEnvVar[];
  status: SandboxStatus;
  remoteInstanceId?: string;
  lastProbeAt?: string;
  lastTransitionReason?: string;
  remark?: string;
  updatedBy: string;
  updatedAt: string;
}

export const mockSandboxes: SandboxItem[] = [
  {
    id: 'sb-001',
    num: 'SB202606131426301234567',
    name: '数据分析-A',
    type: 'CODE',
    provider: 'OPEN_SANDBOX',
    cpu: 1,
    memoryMb: 2048,
    ttlMinutes: 60,
    envVars: [
      { key: 'PYTHONPATH', value: '/workspace/lib', remark: '依赖路径' },
      { key: 'API_TOKEN', value: '***', remark: '外部 API 调用令牌' },
    ],
    status: 'ONLINE',
    remoteInstanceId: 'os-instance-7f23a',
    lastProbeAt: '2026-06-13 15:42:30',
    lastTransitionReason: '探活成功',
    remark: '用于客服 Agent 数据分析场景',
    updatedBy: '张三',
    updatedAt: '2026-06-13 15:42',
  },
  {
    id: 'sb-002',
    num: 'SB202606131539300999999',
    name: '调试-test01',
    type: 'CODE',
    provider: 'OPEN_SANDBOX',
    cpu: 0.5,
    memoryMb: 1024,
    ttlMinutes: 30,
    envVars: [],
    status: 'INITIALIZING',
    lastProbeAt: '2026-06-13 15:39',
    lastTransitionReason: '提交后等待启动',
    updatedBy: '王五',
    updatedAt: '2026-06-13 15:39',
  },
  {
    id: 'sb-003',
    num: 'SB202606131530300888888',
    name: '报表沙箱',
    type: 'CODE',
    provider: 'ALIYUN_SANDBOX',
    cpu: 2,
    memoryMb: 4096,
    ttlMinutes: 120,
    envVars: [{ key: 'OSS_ENDPOINT', value: 'https://oss-cn-hz.aliyuncs.com', remark: 'OSS' }],
    status: 'OFFLINE',
    remoteInstanceId: 'aliyun-7fa1',
    lastProbeAt: '2026-06-13 15:30',
    lastTransitionReason: '探活超时',
    updatedBy: '李四',
    updatedAt: '2026-06-13 15:30',
  },
  {
    id: 'sb-004',
    num: 'SB202606101200300777777',
    name: '老版数据沙箱',
    type: 'CODE',
    provider: 'OPEN_SANDBOX',
    cpu: 1,
    memoryMb: 2048,
    ttlMinutes: 60,
    envVars: [],
    status: 'DISABLED',
    lastTransitionReason: '人工禁用',
    updatedBy: '张三',
    updatedAt: '2026-06-10 12:00',
  },
  {
    id: 'sb-005',
    num: '',
    name: '草稿-未提交',
    type: 'CODE',
    provider: 'OPEN_SANDBOX',
    cpu: 1,
    memoryMb: 2048,
    ttlMinutes: 60,
    envVars: [],
    status: 'DRAFT',
    updatedBy: '张三',
    updatedAt: '2026-06-13 09:00',
  },
];
