/**
 * Skill Mock 数据 —— 用于空间内 Skill 管理设计稿。
 */

export type SkillStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

export interface SkillItem {
  id: string;
  num: string;
  name: string;
  skillKey: string;
  description?: string;
  inputSchema: string;
  outputSchema: string;
  boundAgents: string[];
  status: SkillStatus;
  updatedBy: string;
  updatedAt: string;
}

export const mockSkills: SkillItem[] = [
  {
    id: 'sk-001',
    num: 'SK202606131000300111111',
    name: '客服-FAQ',
    skillKey: 'customer_faq',
    description: '基于 FAQ 知识库回答用户问题',
    inputSchema: '{ "question": "string" }',
    outputSchema: '{ "answer": "string", "confidence": "number" }',
    boundAgents: ['客服总入口'],
    status: 'ENABLED',
    updatedBy: '张三',
    updatedAt: '2026-06-13 10:00',
  },
  {
    id: 'sk-002',
    num: 'SK202606131005300222222',
    name: '订单查询',
    skillKey: 'order_query',
    description: '根据订单号查询订单详情',
    inputSchema: '{ "orderId": "string" }',
    outputSchema: '{ "order": "object" }',
    boundAgents: ['客服总入口'],
    status: 'ENABLED',
    updatedBy: '李四',
    updatedAt: '2026-06-13 10:05',
  },
  {
    id: 'sk-003',
    num: 'SK202606121305300333333',
    name: '数据分析',
    skillKey: 'data_analysis',
    description: '在沙箱中执行 Python 完成数据分析',
    inputSchema: '{ "datasetUrl": "string", "question": "string" }',
    outputSchema: '{ "summary": "string", "chartUrl": "string" }',
    boundAgents: ['数据分析-月报'],
    status: 'ENABLED',
    updatedBy: '王五',
    updatedAt: '2026-06-12 13:05',
  },
  {
    id: 'sk-004',
    num: 'SK202606101000300444444',
    name: '邮件助理',
    skillKey: 'mail_assistant',
    description: '草拟并发送邮件',
    inputSchema: '{ "to": "string", "subject": "string" }',
    outputSchema: '{ "messageId": "string" }',
    boundAgents: [],
    status: 'DRAFT',
    updatedBy: '张三',
    updatedAt: '2026-06-10 10:00',
  },
];
