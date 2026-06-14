import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from '@/pages/Login/LoginPage';
import ForbiddenPage from '@/pages/Forbidden/ForbiddenPage';
import PlatformLayout from '@/layouts/PlatformLayout';
import SpaceLayout from '@/layouts/SpaceLayout';
import PlatformWorkbenchPage from '@/pages/platform/workbench/PlatformWorkbenchPage';
import SpaceListPage from '@/pages/platform/spaces/SpaceListPage';
import UserManagementPage from '@/pages/users/UserManagementPage';
import SystemSettingsPage from '@/pages/platform/system-settings/SystemSettingsPage';
import AgentManagementPage from '@/pages/spaces/agents/AgentManagementPage';
import AgentEditPage from '@/pages/spaces/agents/AgentEditPage';
import SandboxManagementPage from '@/pages/spaces/sandboxes/SandboxManagementPage';
import ModelManagementPage from '@/pages/spaces/models/ModelManagementPage';
import PromptManagementPage from '@/pages/spaces/prompts/PromptManagementPage';
import SkillManagementPage from '@/pages/spaces/skills/SkillManagementPage';
import SkillEditPage from '@/pages/spaces/skills/SkillEditPage';
import ToolManagementPage from '@/pages/spaces/tools/ToolManagementPage';
import ToolEditPage from '@/pages/spaces/tools/ToolEditPage';
import ComingSoonPage from '@/components/ComingSoonPage';
import { AdminGuard, AuthGuard } from '@/routes/Guards';

/**
 * 路由结构：
 * - 平台 Shell：/platform/{workbench|spaces|users|system-settings}
 *   登录默认落地 /platform/workbench（跨空间总览）
 * - 空间 Shell：/spaces/:spaceId/{agents|sandboxes|models|prompts|skills|tools|memory|knowledge|debug/*}
 *   Agent / Skill / 工具 的新建/编辑使用整页路由，不再使用抽屉。
 *   memory / knowledge / debug/agent-debug / debug/agent-evaluation 为待建设占位页。
 */
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<AuthGuard />}>
        <Route index element={<Navigate to="/platform/workbench" replace />} />
        <Route path="/forbidden" element={<ForbiddenPage />} />

        {/* 平台 Shell */}
        <Route path="/platform" element={<PlatformLayout />}>
          <Route index element={<Navigate to="workbench" replace />} />
          <Route path="workbench" element={<PlatformWorkbenchPage />} />
          <Route path="spaces" element={<SpaceListPage />} />
          <Route element={<AdminGuard />}>
            <Route path="users" element={<UserManagementPage />} />
            <Route path="system-settings" element={<SystemSettingsPage />} />
          </Route>
        </Route>

        {/* 空间 Shell */}
        <Route path="/spaces/:spaceId" element={<SpaceLayout />}>
          <Route index element={<Navigate to="agents" replace />} />
          {/* 兼容旧的 /spaces/:id/dashboard 链接 */}
          <Route path="dashboard" element={<Navigate to="../agents" relative="path" replace />} />

          {/* Agent */}
          <Route path="agents" element={<AgentManagementPage />} />
          <Route path="agents/new" element={<AgentEditPage />} />
          <Route path="agents/:agentId/edit" element={<AgentEditPage />} />

          {/* 沙箱 */}
          <Route path="sandboxes" element={<SandboxManagementPage />} />

          {/* 模型与工具 */}
          <Route path="models" element={<ModelManagementPage />} />
          <Route path="prompts" element={<PromptManagementPage />} />

          <Route path="skills" element={<SkillManagementPage />} />
          <Route path="skills/new" element={<SkillEditPage />} />
          <Route path="skills/:skillId/edit" element={<SkillEditPage />} />

          <Route path="tools" element={<ToolManagementPage />} />
          <Route path="tools/new" element={<ToolEditPage />} />
          <Route path="tools/:toolId/edit" element={<ToolEditPage />} />

          {/* 待建设 */}
          <Route
            path="memory"
            element={
              <ComingSoonPage
                title="记忆管理"
                description="Agent 长短期记忆资产 · 子模块建设中"
              />
            }
          />
          <Route
            path="knowledge"
            element={
              <ComingSoonPage
                title="知识库管理"
                description="向量知识库 / 检索增强能力 · 子模块建设中"
              />
            }
          />
          <Route
            path="debug/agent-debug"
            element={
              <ComingSoonPage
                title="Agent 调试"
                description="单次调用、链路 Trace、工具回放 · 子模块建设中"
              />
            }
          />
          <Route
            path="debug/agent-evaluation"
            element={
              <ComingSoonPage
                title="Agent 评测"
                description="评测集 / 跑批 / 指标对比 · 子模块建设中"
              />
            }
          />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/platform/workbench" replace />} />
    </Routes>
  );
}
