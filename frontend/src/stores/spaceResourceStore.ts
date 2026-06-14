import { create } from 'zustand';
import * as agentApi from '@/api/agent';
import * as skillApi from '@/api/skill';
import * as toolApi from '@/api/tool';
import type { AgentDTO, AssemblySnapshotDTO } from '@/api/agent';
import type { SkillDTO, SkillVersionDTO } from '@/api/skill';
import type { ToolDTO } from '@/api/tool';

export interface AgentItem extends AgentDTO {}
export interface SkillItem extends SkillDTO {}
export interface ToolItem extends ToolDTO {}

interface SpaceResourceState {
  agents: AgentItem[];
  skills: SkillItem[];
  tools: ToolItem[];
  loading: Record<string, boolean>;

  fetchAgents: (spaceCode: string) => Promise<void>;
  fetchSkills: (spaceCode: string) => Promise<void>;
  fetchTools: (spaceCode: string) => Promise<void>;

  createAgent: (spaceCode: string, name: string, assembly: AssemblySnapshotDTO) => Promise<AgentDTO>;
  createSkill: (spaceCode: string, name: string, description: string, skillMd?: string) => Promise<SkillDTO>;
  createTool: (spaceCode: string, name: string, type: string, subType: string, configJson: string) => Promise<ToolDTO>;

  removeAgent: (num: string) => Promise<void>;
  removeSkill: (num: string) => Promise<void>;
  removeTool: (num: string) => Promise<void>;
}

export const useSpaceResourceStore = create<SpaceResourceState>((set, get) => ({
  agents: [],
  skills: [],
  tools: [],
  loading: {},

  async fetchAgents(spaceCode) {
    set((s) => ({ loading: { ...s.loading, agents: true } }));
    try {
      const agents = await agentApi.listAgentVersions(spaceCode);
      set({ agents: agents as unknown as AgentItem[] });
    } finally {
      set((s) => ({ loading: { ...s.loading, agents: false } }));
    }
  },

  async fetchSkills(spaceCode) {
    set((s) => ({ loading: { ...s.loading, skills: true } }));
    try {
      const skills = await skillApi.listEffectiveSkills(spaceCode);
      set({ skills: skills as SkillItem[] });
    } finally {
      set((s) => ({ loading: { ...s.loading, skills: false } }));
    }
  },

  async fetchTools(spaceCode) {
    set((s) => ({ loading: { ...s.loading, tools: true } }));
    try {
      const tools = await toolApi.listEffectiveTools(spaceCode);
      set({ tools: tools as ToolItem[] });
    } finally {
      set((s) => ({ loading: { ...s.loading, tools: false } }));
    }
  },

  async createAgent(spaceCode, name, assembly) {
    const result = await agentApi.createAgent(spaceCode, name, name, '', [], '', '1.0.0', assembly);
    set((s) => ({ agents: [result as unknown as AgentItem, ...s.agents] }));
    return result;
  },

  async createSkill(spaceCode, name, description, skillMd) {
    const result = await skillApi.createSkill(spaceCode, name, description, [], '', skillMd);
    set((s) => ({ skills: [result as SkillItem, ...s.skills] }));
    return result;
  },

  async createTool(spaceCode, name, type, subType, configJson) {
    const result = await toolApi.createTool(spaceCode, name, type, subType, configJson);
    set((s) => ({ tools: [result as ToolItem, ...s.tools] }));
    return result;
  },

  async removeAgent(num) {
    await agentApi.deleteAgent(num);
    set((s) => ({ agents: s.agents.filter((a) => a.num !== num) }));
  },

  async removeSkill(num) {
    await skillApi.deleteSkill(num);
    set((s) => ({ skills: s.skills.filter((a) => a.num !== num) }));
  },

  async removeTool(num) {
    await toolApi.deleteTool(num);
    set((s) => ({ tools: s.tools.filter((a) => a.num !== num) }));
  },
}));