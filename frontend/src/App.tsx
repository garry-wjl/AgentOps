import { Navigate, Route, Routes } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import LoginPage from '@/pages/Login/LoginPage';
import SpaceHomePage from '@/pages/spaces/dashboard/SpaceHomePage';
import SystemSettingsPage from '@/pages/system-settings/SystemSettingsPage';
import UserManagementPage from '@/pages/users/UserManagementPage';
import ForbiddenPage from '@/pages/Forbidden/ForbiddenPage';
import { AdminGuard, AuthGuard } from '@/routes/Guards';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<AuthGuard />}>
        <Route element={<MainLayout />}>
          <Route index element={<Navigate to="/spaces" replace />} />
          <Route path="/spaces" element={<SpaceHomePage />} />
          <Route path="/forbidden" element={<ForbiddenPage />} />
          <Route element={<AdminGuard />}>
            <Route path="/users" element={<UserManagementPage />} />
            <Route path="/system-settings" element={<SystemSettingsPage />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/spaces" replace />} />
    </Routes>
  );
}
