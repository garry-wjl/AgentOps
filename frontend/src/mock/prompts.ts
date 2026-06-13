/**
 * Prompt Mock 数据 —— 对应《Prompt 管理 PRD》。
 */

export type PromptStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

export interface PromptItem {
  id: string;
  num: string;
  name: string;
  promptKey: string;
  content: string;
  variables: string[];
  status: PromptStatus;
  remark?: string;
  updatedBy: string;
  updatedAt: string;
}

export const mockPrompts: PromptItem[] = [
  {
    id: 'pr-001',
    num: 'PR202606131426301234567',
    name: '客服-开场白',
    promptKey: 'customer_service_opening',
    content: `你是一名{{role}}，请用{{language}}回答用户问题。

用户问题：
{{user_question}}

请按以下结构回答：
1. 关键结论
2. 详细说明`,
    variables: ['role', 'language', 'user_question'],
    status: 'ENABLED',
    remark: '用于客服 Agent 的开场白',
    updatedBy: '张三',
    updatedAt: '2026-06-13 15:42',
  },
  {
    id: 'pr-002',
    num: 'PR202606131401300555555',
    name: 'FAQ-保险类',
    promptKey: 'faq_insurance',
    content: `请基于以下知识库回答关于保险产品的问题：

{{knowledge_base}}

用户问题：{{question}}`,
    variables: ['knowledge_base', 'question'],
    status: 'ENABLED',
    remark: '保险类 FAQ',
    updatedBy: '李四',
    updatedAt: '2026-06-13 14:01',
  },
  {
    id: 'pr-003',
    num: 'PR202606131320300666666',
    name: '调试用-test01',
    promptKey: 'test01',
    content: `调试中的提示词，{{var1}} {{var2}}`,
    variables: ['var1', 'var2'],
    status: 'DRAFT',
    updatedBy: '张三',
    updatedAt: '2026-06-13 13:20',
  },
  {
    id: 'pr-004',
    num: 'PR202606120911300777777',
    name: '旧版开场白',
    promptKey: 'legacy_opening',
    content: `（旧版）你好我是助理 {{name}}`,
    variables: ['name'],
    status: 'DISABLED',
    updatedBy: '张三',
    updatedAt: '2026-06-12 09:11',
  },
];
