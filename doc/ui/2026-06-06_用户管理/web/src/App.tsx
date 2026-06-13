import { ApartmentOutlined, IdcardOutlined, LogoutOutlined, SafetyCertificateOutlined, SettingOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons';
import { ProLayout } from '@ant-design/pro-components';
import { Avatar, Button, Divider, Dropdown, Result, Space, Tag, Typography } from 'antd';
import { useMemo, useState } from 'react';
import LoginPage from './pages/Login/LoginPage';
import SpaceHomePage from './pages/SpaceHome/SpaceHomePage';
import SystemSettingsPage from './pages/SystemSettings/SystemSettingsPage';
import UserManagementPage from './pages/UserManagement/UserManagementPage';
import type { CurrentUser } from './types/user';
import { isAdmin } from './utils/user';

type RouteKey = '/spaces' | '/users' | '/system-settings';

export default function App() {
  const [currentUser, setCurrentUser] = useState<CurrentUser>();
  const [pathname, setPathname] = useState<RouteKey>('/spaces');

  const admin = useMemo(() => (currentUser ? isAdmin(currentUser.roles) : false), [currentUser]);

  if (!currentUser) {
    return <LoginPage onLogin={(user) => { setCurrentUser(user); setPathname('/spaces'); }} />;
  }

  const route = {
    path: '/',
    routes: [
      {
        path: '/spaces',
        name: '空间管理',
        icon: <ApartmentOutlined />,
      },
      ...(admin
        ? [
            {
              path: '/users',
              name: '用户管理',
              icon: <TeamOutlined />,
            },
            {
              path: '/system-settings',
              name: '系统设置',
              icon: <SettingOutlined />,
            },
          ]
        : []),
    ],
  };

  const renderPage = () => {
    if (pathname === '/users') {
      return admin ? <UserManagementPage /> : <NoPermission />;
    }
    if (pathname === '/system-settings') {
      return admin ? <SystemSettingsPage /> : <NoPermission />;
    }
    return <SpaceHomePage currentUser={currentUser} />;
  };

  return (
    <ProLayout
      title="AgentOps"
      logo="https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg"
      route={route}
      location={{ pathname }}
      layout="mix"
      splitMenus={false}
      menuItemRender={(item, dom) => (
        <div onClick={() => setPathname(item.path as RouteKey)}>{dom}</div>
      )}
      actionsRender={() => [
        <UserProfileDropdown
          key="profile"
          currentUser={currentUser}
          onLogout={() => {
            setCurrentUser(undefined);
            setPathname('/spaces');
          }}
        />,
      ]}
      token={{
        header: { colorBgHeader: '#ffffff' },
        sider: { colorMenuBackground: '#ffffff' },
      }}
    >
      {renderPage()}
    </ProLayout>
  );
}

interface UserProfileDropdownProps {
  currentUser: CurrentUser;
  onLogout: () => void;
}

function UserProfileDropdown({ currentUser, onLogout }: UserProfileDropdownProps) {
  const admin = isAdmin(currentUser.roles);
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
              <Tag key={role} color={role === '管理员' ? 'blue' : 'green'}>
                {role}
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

          <div className="volc-account-actions">
            <button type="button" className="volc-account-action-item">
              <UserOutlined />
              <span>账号信息</span>
            </button>
            <button type="button" className="volc-account-action-item">
              <SafetyCertificateOutlined />
              <span>安全设置</span>
            </button>
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
          <span className="user-profile-name">{currentUser.name}</span>
          <span className="user-profile-subtitle">{primaryRole}</span>
        </span>
      </button>
    </Dropdown>
  );
}

function NoPermission() {
  return <Result status="403" title="无权限访问该功能" subTitle="普通用户不能使用用户管理和系统设置功能。" />;
}
