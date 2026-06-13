/**
 * 工具 Mock 数据 —— 用于空间内工具管理设计稿。
 */

export type ToolType = 'MCP' | 'FUNCTION_CALL';
export type ToolStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

export interface ToolItem {
  id: string;
  num: string;
  name: string;
  toolKey: string;
  type: ToolType;
  endpoint?: string;
  authType: 'NONE' | 'API_KEY' | 'OAUTH';
  description?: string;
  status: ToolStatus;
  health: 'HEALTHY' | 'DEGRADED' | 'UNKNOWN';
  updatedBy: string;
  updatedAt: string;
}

export const mockTools: ToolItem[] = [
  {
    id: 'tl-001',
    num: 'TL202606131000300111111',
    name: '查询订单',
    toolKey: 'query_order',
    type: 'MCP',
    endpoint: 'https://mcp.internal/order',
    authType: 'API_KEY',
    description: '通过 MCP 协议查询订单',
    status: 'ENABLED',
    health: 'HEALTHY',
    updatedBy: '张三',
    updatedAt: '2026-06-13 10:00',
  },
  {
    id: 'tl-002',
    num: 'TL202606131005300222222',
    name: '发送邮件',
    toolKey: 'send_mail',
    type: 'FUNCTION_CALL',
    authType: 'NONE',
    description: '通过 Function Call 发送邮件',
    status: 'ENABLED',
    health: 'HEALTHY',
    updatedBy: '李四',
    updatedAt: '2026-06-13 10:05',
  },
  {
    id: 'tl-003',
    num: 'TL202606121505300333333',
    name: 'BI 查询',
    toolKey: 'bi_query',
    type: 'MCP',
    endpoint: 'https://bi.internal/mcp',
    authType: 'OAUTH',
    description: '查询 BI 平台报表数据',
    status: 'ENABLED',
    health: 'DEGRADED',
    updatedBy: '王五',
    updatedAt: '2026-06-12 15:05',
  },
  {
    id: 'tl-004',
    num: 'TL202606101000300444444',
    name: '查询天气',
    toolKey: 'weather_lookup',
    type: 'FUNCTION_CALL',
    authType: 'API_KEY',
    description: '调用第三方天气接口',
    status: 'DISABLED',
    health: 'UNKNOWN',
    updatedBy: '张三',
    updatedAt: '2026-06-10 10:00',
  },
];
