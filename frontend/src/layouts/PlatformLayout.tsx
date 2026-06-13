import {
  ApartmentOutlined,
  SettingOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import BrandHeader from '@/layouts/BrandHeader';
import { hasAdminRole, useAuthStore } from '@/stores/authStore';

const { Sider, Content } = Layout;

/**
 * 平台 Shell：顶部 Logo + 用户菜单；左侧 = 空间管理 / 用户管理（仅管理员）/ 系统设置（仅管理员）。
 */
export default function PlatformLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const currentUser = useAuthStore((s) => s.currentUser);
  if (!currentUser) return null;
  const admin = hasAdminRole(currentUser);

  const items = [
    { key: '/platform/spaces', icon: <ApartmentOutlined />, label: '空间管理' },
    ...(admin
      ? [
          { key: '/platform/users', icon: <TeamOutlined />, label: '用户管理' },
          { key: '/platform/system-settings', icon: <SettingOutlined />, label: '系统设置' },
        ]
      : []),
  ];

  // 把 /platform/users/xxx 之类的子路径回归到一级 key
  const selectedKey = items.find((i) => location.pathname.startsWith(i.key))?.key;

  return (
    <Layout className="shell-layout">
      <BrandHeader onLogoClick={() => navigate('/platform/spaces')} />
      <Layout className="shell-body">
        <Sider width={208} className="shell-sider" theme="light">
          <Menu
            mode="inline"
            selectedKeys={selectedKey ? [selectedKey] : []}
            items={items}
            onClick={({ key }) => navigate(key)}
            className="shell-menu"
          />
        </Sider>
        <Content className="shell-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
