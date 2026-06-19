/**
 * 模型 Mock 数据 —— 用于空间内模型管理设计稿。
 */

export type ModelStatus = 'DRAFT' | 'ENABLED' | 'DISABLED';

export interface ModelItem {
  id: string;
  num: string;
  name: string;
  provider: 'OPENAI' | 'ANTHROPIC' | 'AZURE' | 'OLLAMA' | 'CUSTOM';
  modelId: string;
  endpoint?: string;
  hasApiKey: boolean;
  presetTemperature: number;
  presetMaxTokens: number;
  status: ModelStatus;
  health: 'HEALTHY' | 'DEGRADED' | 'UNKNOWN';
  updatedAt: string;
}

export const mockModels: ModelItem[] = [
  {
    id: 'mo-001',
    num: 'MO202606131000300111111',
    name: 'OpenAI GPT-4o',
    provider: 'OPENAI',
    modelId: 'gpt-4o',
    endpoint: 'https://api.openai.com/v1',
    hasApiKey: true,
    presetTemperature: 0.7,
    presetMaxTokens: 4096,
    status: 'ENABLED',
    health: 'HEALTHY',
    updatedAt: '2026-06-13 10:00',
  },
  {
    id: 'mo-002',
    num: 'MO202606131005300222222',
    name: 'Claude Sonnet 4.6',
    provider: 'ANTHROPIC',
    modelId: 'claude-sonnet-4-6',
    endpoint: 'https://api.anthropic.com',
    hasApiKey: true,
    presetTemperature: 0.5,
    presetMaxTokens: 8192,
    status: 'ENABLED',
    health: 'HEALTHY',
    updatedAt: '2026-06-13 10:05',
  },
  {
    id: 'mo-003',
    num: 'MO202606121505300333333',
    name: 'Azure GPT-4o-mini',
    provider: 'AZURE',
    modelId: 'gpt-4o-mini',
    endpoint: 'https://example.openai.azure.com',
    hasApiKey: true,
    presetTemperature: 0.3,
    presetMaxTokens: 2048,
    status: 'ENABLED',
    health: 'DEGRADED',
    updatedAt: '2026-06-12 15:05',
  },
  {
    id: 'mo-004',
    num: 'MO202606101000300444444',
    name: '本地 Ollama-Llama3',
    provider: 'OLLAMA',
    modelId: 'llama3:8b',
    endpoint: 'http://localhost:11434',
    hasApiKey: false,
    presetTemperature: 0.7,
    presetMaxTokens: 2048,
    status: 'DISABLED',
    health: 'UNKNOWN',
    updatedAt: '2026-06-10 10:00',
  },
];
