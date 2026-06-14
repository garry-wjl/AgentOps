import { create } from 'zustand';
import { mockAgents, type AgentItem } from '@/mock/agents';
import { mockSkills, type SkillItem } from '@/mock/skills';
import { mockTools, type ToolItem } from '@/mock/tools';

/**
 * 空间内资源（Agent / Skill / 工具）的全局状态。
 *
 * 这三个模块的「新建/编辑」改成了独立路由页，列表与编辑页之间需要共享数据，
 * 因此从原来的 useState(mockX) 升级到 zustand store。
 *
 * 当对接真实后端时，这里改成调用 API 即可，列表与编辑页代码不变。
 */
interface SpaceResourceState {
  agents: AgentItem[];
  skills: SkillItem[];
  tools: ToolItem[];
  upsertAgent: (a: AgentItem) => void;
  upsertSkill: (s: SkillItem) => void;
  upsertTool: (t: ToolItem) => void;
  removeAgent: (id: string) => void;
  removeSkill: (id: string) => void;
  removeTool: (id: string) => void;
}

export const useSpaceResourceStore = create<SpaceResourceState>((set) => ({
  agents: mockAgents,
  skills: mockSkills,
  tools: mockTools,
  upsertAgent: (a) =>
    set((state) => {
      const idx = state.agents.findIndex((x) => x.id === a.id);
      if (idx === -1) return { agents: [a, ...state.agents] };
      const next = state.agents.slice();
      next[idx] = a;
      return { agents: next };
    }),
  upsertSkill: (s) =>
    set((state) => {
      const idx = state.skills.findIndex((x) => x.id === s.id);
      if (idx === -1) return { skills: [s, ...state.skills] };
      const next = state.skills.slice();
      next[idx] = s;
      return { skills: next };
    }),
  upsertTool: (t) =>
    set((state) => {
      const idx = state.tools.findIndex((x) => x.id === t.id);
      if (idx === -1) return { tools: [t, ...state.tools] };
      const next = state.tools.slice();
      next[idx] = t;
      return { tools: next };
    }),
  removeAgent: (id) => set((state) => ({ agents: state.agents.filter((a) => a.id !== id) })),
  removeSkill: (id) => set((state) => ({ skills: state.skills.filter((s) => s.id !== id) })),
  removeTool: (id) => set((state) => ({ tools: state.tools.filter((t) => t.id !== id) })),
}));
