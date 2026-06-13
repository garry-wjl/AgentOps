/**
 * Agent Mock 数据 —— 用于空间内 Agent 列表与详情设计稿。
 */

export type AgentStatus = 'DRAFT' | 'ONLINE' | 'OFFLINE';

export interface AgentItem {
  id: string;
  num: string;
  name: string;
  description?: string;
  modelName: string;
  promptKey: string;
  skills: string[];
  tools: string[];
  sandboxName?: string;
  status: AgentStatus;
  updatedBy: string;
  updatedAt: string;
}

export const mockAgents: AgentItem[] = [
  {
    id: 'ag-001',
    num: 'AG202606131426301111111',
    name: '客服总入口',
    description: '统一接收用户咨询并分发到子流程',
    modelName: 'GPT-4o',
    promptKey: 'customer_service_opening',
    skills: ['客服-FAQ', '订单查询'],
    tools: ['查询订单(MCP)', '发送邮件(Function)'],
    sandboxName: undefined,
    status: 'ONLINE',
    updatedBy: '张三',
    updatedAt: '2026-06-13 15:42',
  },
  {
    id: 'ag-002',
    num: 'AG202606131027300222222',
    name: '数据分析-月报',
    description: '基于 SQL 与代码沙箱出月度数据报告',
    modelName: 'Claude Sonnet 4.6',
    promptKey: 'monthly_report',
    skills: ['数据分析'],
    tools: ['BI 查询(MCP)'],
    sandboxName: '数据分析-A',
    status: 'ONLINE',
    updatedBy: '李四',
    updatedAt: '2026-06-13 11:20',
  },
  {
    id: 'ag-003',
    num: 'AG202606121627300333333',
    name: '调试中-客服 V2',
    description: '客服总入口 V2，正在打磨提示词',
    modelName: 'GPT-4o-mini',
    promptKey: 'customer_service_opening_v2',
    skills: ['客服-FAQ'],
    tools: [],
    status: 'DRAFT',
    updatedBy: '张三',
    updatedAt: '2026-06-12 16:27',
  },
  {
    id: 'ag-004',
    num: 'AG202606081020300444444',
    name: '邮件助理-旧版',
    description: '已下线',
    modelName: 'GPT-4o-mini',
    promptKey: 'mail_assistant_v1',
    skills: ['邮件助理'],
    tools: ['发送邮件(Function)'],
    status: 'OFFLINE',
    updatedBy: '王五',
    updatedAt: '2026-06-08 10:20',
  },
];
