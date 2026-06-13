import { useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { Spin } from 'antd';
import { hasAdminRole, useAuthStore } from '@/stores/authStore';

/**
 * 已登录守卫：未登录 → /login。
 */
export function AuthGuard() {
  const location = useLocation();
  const { token, currentUser, initialized, fetchCurrent } = useAuthStore();

  useEffect(() => {
    if (token && !currentUser && !initialized) {
      void fetchCurrent();
    } else if (!token) {
      useAuthStore.setState({ initialized: true });
    }
  }, [token, currentUser, initialized, fetchCurrent]);

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (!initialized || !currentUser) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  return <Outlet />;
}

/**
 * 管理员守卫：非管理员 → /forbidden。
 */
export function AdminGuard() {
  const { currentUser } = useAuthStore();
  if (!hasAdminRole(currentUser)) {
    return <Navigate to="/forbidden" replace />;
  }
  return <Outlet />;
}
