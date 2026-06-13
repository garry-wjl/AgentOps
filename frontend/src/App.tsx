import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from '@/pages/Login/LoginPage';
import ForbiddenPage from '@/pages/Forbidden/ForbiddenPage';
import PlatformLayout from '@/layouts/PlatformLayout';
import SpaceLayout from '@/layouts/SpaceLayout';
import SpaceListPage from '@/pages/platform/spaces/SpaceListPage';
import UserManagementPage from '@/pages/users/UserManagementPage';
import SystemSettingsPage from '@/pages/platform/system-settings/SystemSettingsPage';
import SpaceWorkbenchPage from '@/pages/spaces/workbench/SpaceWorkbenchPage';
import AgentManagementPage from '@/pages/spaces/agents/AgentManagementPage';
import SandboxManagementPage from '@/pages/spaces/sandboxes/SandboxManagementPage';
import ModelManagementPage from '@/pages/spaces/models/ModelManagementPage';
import PromptManagementPage from '@/pages/spaces/prompts/PromptManagementPage';
import SkillManagementPage from '@/pages/spaces/skills/SkillManagementPage';
import ToolManagementPage from '@/pages/spaces/tools/ToolManagementPage';
import DebugPlaceholderPage from '@/pages/spaces/debug/DebugPlaceholderPage';
import SpaceMembersPage from '@/pages/spaces/members/SpaceMembersPage';
import { AdminGuard, AuthGuard } from '@/routes/Guards';

/**
 * 路由结构对齐《UI 信息架构与导航规范》：
 * - 平台 Shell：/platform/{spaces|users|system-settings}
 * - 空间 Shell：/spaces/:spaceId/{dashboard|agents|sandboxes|models|prompts|skills|tools|debug|members}
 */
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<AuthGuard />}>
        <Route index element={<Navigate to="/platform/spaces" replace />} />
        <Route path="/forbidden" element={<ForbiddenPage />} />

        {/* 平台 Shell */}
        <Route path="/platform" element={<PlatformLayout />}>
          <Route index element={<Navigate to="spaces" replace />} />
          <Route path="spaces" element={<SpaceListPage />} />
          <Route element={<AdminGuard />}>
            <Route path="users" element={<UserManagementPage />} />
            <Route path="system-settings" element={<SystemSettingsPage />} />
          </Route>
        </Route>

        {/* 空间 Shell */}
        <Route path="/spaces/:spaceId" element={<SpaceLayout />}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<SpaceWorkbenchPage />} />
          <Route path="agents" element={<AgentManagementPage />} />
          <Route path="sandboxes" element={<SandboxManagementPage />} />
          <Route path="models" element={<ModelManagementPage />} />
          <Route path="prompts" element={<PromptManagementPage />} />
          <Route path="skills" element={<SkillManagementPage />} />
          <Route path="tools" element={<ToolManagementPage />} />
          <Route path="debug" element={<DebugPlaceholderPage />} />
          <Route path="members" element={<SpaceMembersPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/platform/spaces" replace />} />
    </Routes>
  );
}
