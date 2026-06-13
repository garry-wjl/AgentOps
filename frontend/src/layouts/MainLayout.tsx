import {
  ApartmentOutlined,
  IdcardOutlined,
  LogoutOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { ProLayout } from '@ant-design/pro-components';
import { Avatar, Button, Divider, Dropdown, Space, Tag, Typography } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { hasAdminRole, useAuthStore } from '@/stores/authStore';
import type { CurrentUserVO } from '@/types/api';

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const currentUser = useAuthStore((s) => s.currentUser);
  const logout = useAuthStore((s) => s.logout);

  if (!currentUser) {
    return null;
  }
  const admin = hasAdminRole(currentUser);

  const route = {
    path: '/',
    routes: [
      { path: '/spaces', name: '空间管理', icon: <ApartmentOutlined /> },
      ...(admin
        ? [
            { path: '/users', name: '用户管理', icon: <TeamOutlined /> },
            { path: '/system-settings', name: '系统设置', icon: <SettingOutlined /> },
          ]
        : []),
    ],
  };

  return (
    <ProLayout
      title="AgentOps"
      logo="https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg"
      route={route}
      location={{ pathname: location.pathname }}
      layout="mix"
      splitMenus={false}
      menuItemRender={(item, dom) => (
        <div onClick={() => item.path && navigate(item.path)}>{dom}</div>
      )}
      actionsRender={() => [
        <UserProfileDropdown
          key="profile"
          currentUser={currentUser}
          onLogout={async () => {
            await logout();
            navigate('/login', { replace: true });
          }}
        />,
      ]}
      token={{
        header: { colorBgHeader: '#ffffff' },
        sider: { colorMenuBackground: '#ffffff' },
      }}
    >
      <Outlet />
    </ProLayout>
  );
}

interface UserProfileDropdownProps {
  currentUser: CurrentUserVO;
  onLogout: () => void;
}

function UserProfileDropdown({ currentUser, onLogout }: UserProfileDropdownProps) {
  const admin = hasAdminRole(currentUser);
  const primaryRole = admin ? '管理员' : '普通用户';
  const permissionText = admin ? '空间管理 / 用户管理 / 系统设置' : '空间管理';

  return (
    <Dropdown
      trigger={['click']}
      placement="bottomRight"
      dropdownRender={() => (
        <div className="user-profile-dropdown">
          <div className="user-profile-header">
            <Avatar size={44} className="user-profile-avatar" icon={<UserOutlined />} />
            <div className="user-profile-meta">
              <Typography.Text strong>{currentUser.name}</Typography.Text>
              <Typography.Text type="secondary" className="user-profile-role">
                {primaryRole}
              </Typography.Text>
            </div>
          </div>

          <div className="user-profile-tags">
            {currentUser.roles.map((role) => (
              <Tag key={role.code} color={role.code === 'ADMIN' ? 'blue' : 'green'}>
                {role.label}
              </Tag>
            ))}
          </div>

          <div className="user-profile-info volc-account-section">
            <div className="user-profile-info-item">
              <SafetyCertificateOutlined />
              <span>权限范围</span>
              <Typography.Text>{permissionText}</Typography.Text>
            </div>
            <div className="user-profile-info-item">
              <IdcardOutlined />
              <span>账号类型</span>
              <Typography.Text>{primaryRole}</Typography.Text>
            </div>
            <div className="user-profile-info-item">
              <UserOutlined />
              <span>账号状态</span>
              <Typography.Text type="success">已启用</Typography.Text>
            </div>
          </div>

          <Divider style={{ margin: '8px 0' }} />

          <Button block icon={<LogoutOutlined />} onClick={onLogout} className="volc-account-logout">
            退出登录
          </Button>
        </div>
      )}
    >
      <button className="user-profile-trigger" type="button">
        <Avatar size={32} className="user-profile-avatar" icon={<UserOutlined />} />
        <span className="user-profile-trigger-text">
          <Space size={2} direction="vertical">
            <span className="user-profile-name">{currentUser.name}</span>
            <span className="user-profile-subtitle">{primaryRole}</span>
          </Space>
        </span>
      </button>
    </Dropdown>
  );
}
