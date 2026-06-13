import { LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Divider, Dropdown, Tag, Typography } from 'antd';
import { hasAdminRole, useAuthStore } from '@/stores/authStore';
import type { CurrentUserVO } from '@/types/api';

interface BrandHeaderProps {
  /** 顶部右侧扩展区，例如空间下拉，可选 */
  extra?: React.ReactNode;
  /** 顶部中间区域，例如面包屑/空间下拉，可选 */
  center?: React.ReactNode;
  /** 点击 Logo 的行为 */
  onLogoClick?: () => void;
}

/**
 * 全平台共用的顶部品牌条：左 Logo + 平台名，中间为可插槽，右侧用户菜单。
 */
export default function BrandHeader({ extra, center, onLogoClick }: BrandHeaderProps) {
  const currentUser = useAuthStore((s) => s.currentUser);
  const logout = useAuthStore((s) => s.logout);

  return (
    <div className="brand-header">
      <div className="brand-header-left" onClick={onLogoClick} role="button">
        <div className="brand-logo">A</div>
        <span className="brand-text">AgentOps</span>
      </div>
      {center && <div className="brand-header-center">{center}</div>}
      <div className="brand-header-right">
        {extra}
        {currentUser && <UserMenu currentUser={currentUser} onLogout={() => logout()} />}
      </div>
    </div>
  );
}

interface UserMenuProps {
  currentUser: CurrentUserVO;
  onLogout: () => void;
}

function UserMenu({ currentUser, onLogout }: UserMenuProps) {
  const admin = hasAdminRole(currentUser);
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
                {admin ? '管理员' : '普通用户'}
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
          <Divider style={{ margin: '8px 0' }} />
          <button className="user-profile-action" onClick={() => alert('个人信息（待实现）')}>
            个人信息
          </button>
          <button className="user-profile-action" onClick={() => alert('修改密码（待实现）')}>
            修改密码
          </button>
          <Divider style={{ margin: '8px 0' }} />
          <button className="user-profile-action user-profile-logout" onClick={onLogout}>
            <LogoutOutlined /> 退出登录
          </button>
        </div>
      )}
    >
      <button className="user-profile-trigger" type="button" aria-label={currentUser.name}>
        <Avatar size={32} icon={<UserOutlined />} />
      </button>
    </Dropdown>
  );
}
