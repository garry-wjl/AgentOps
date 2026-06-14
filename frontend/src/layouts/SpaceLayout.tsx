import {
  AppstoreOutlined,
  ApiOutlined,
  ApartmentOutlined,
  BookOutlined,
  BugOutlined,
  CodeOutlined,
  DatabaseOutlined,
  DownOutlined,
  ExperimentOutlined,
  FileTextOutlined,
  FundOutlined,
  HomeOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import { Dropdown, Layout, Menu, Tag } from 'antd';
import type { MenuProps } from 'antd';
import { Outlet, useLocation, useNavigate, useParams } from 'react-router-dom';
import BrandHeader from '@/layouts/BrandHeader';
import { findSpace, mockSpaces } from '@/mock/spaces';

const { Sider, Content } = Layout;

/**
 * 空间 Shell：顶部 Logo + 当前空间下拉（切换空间）+ 用户菜单；
 * 左侧分组：Agent 与沙箱 / 模型与工具 / 调试与评测。
 */
export default function SpaceLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { spaceId = '' } = useParams();
  const space = findSpace(spaceId) ?? mockSpaces[0];

  const items: MenuProps['items'] = [
    {
      key: 'group-agent',
      type: 'group',
      label: 'Agent 与沙箱',
      children: [
        {
          key: `/spaces/${spaceId}/agents`,
          icon: <RobotOutlined />,
          label: 'Agent 管理',
        },
        {
          key: `/spaces/${spaceId}/sandboxes`,
          icon: <CodeOutlined />,
          label: '沙箱管理',
        },
      ],
    },
    {
      key: 'group-tools',
      type: 'group',
      label: '模型与工具',
      children: [
        {
          key: `/spaces/${spaceId}/models`,
          icon: <ApiOutlined />,
          label: '模型管理',
        },
        {
          key: `/spaces/${spaceId}/prompts`,
          icon: <FileTextOutlined />,
          label: 'Prompt 管理',
        },
        {
          key: `/spaces/${spaceId}/skills`,
          icon: <ThunderboltOutlined />,
          label: 'Skill 管理',
        },
        {
          key: `/spaces/${spaceId}/tools`,
          icon: <ToolOutlined />,
          label: '工具管理',
        },
        {
          key: `/spaces/${spaceId}/memory`,
          icon: <DatabaseOutlined />,
          label: '记忆管理',
        },
        {
          key: `/spaces/${spaceId}/knowledge`,
          icon: <BookOutlined />,
          label: '知识库管理',
        },
      ],
    },
    {
      key: 'group-debug',
      type: 'group',
      label: '调试与评测',
      children: [
        {
          key: `/spaces/${spaceId}/debug/agent-debug`,
          icon: <BugOutlined />,
          label: 'Agent 调试',
        },
        {
          key: `/spaces/${spaceId}/debug/agent-evaluation`,
          icon: <FundOutlined />,
          label: 'Agent 评测',
        },
      ],
    },
  ];

  // 找到与当前 URL 匹配的最长 key
  const allKeys = collectKeys(items);
  const selected = allKeys
    .filter((k) => location.pathname.startsWith(k))
    .sort((a, b) => b.length - a.length)[0];

  const switchItems = mockSpaces.map((s) => ({
    key: s.id,
    label: (
      <div className="space-switch-item">
        <span>{s.name}</span>
        <Tag color={s.myRole === 'ADMIN' ? 'blue' : 'green'} bordered={false}>
          {s.myRole === 'ADMIN' ? '管理' : '成员'}
        </Tag>
      </div>
    ),
  }));

  return (
    <Layout className="shell-layout">
      <BrandHeader
        onLogoClick={() => navigate('/platform/workbench')}
        leftExtras={
          <>
            <button
              type="button"
              className="header-link-button"
              onClick={() => navigate('/platform/workbench')}
            >
              <HomeOutlined />
              <span>首页</span>
            </button>
            <span className="header-breadcrumb-sep" aria-hidden>
              /
            </span>
            <Dropdown
              menu={{
                items: switchItems,
                onClick: ({ key }) => navigate(`/spaces/${key}/agents`),
              }}
              trigger={['click']}
            >
              <button type="button" className="header-link-button header-link-button--space">
                <ApartmentOutlined />
                <span>{space.name}</span>
                <DownOutlined className="header-link-button-arrow" />
              </button>
            </Dropdown>
          </>
        }
      />
      <Layout className="shell-body">
        <Sider width={236} className="shell-sider" theme="light">
          <Menu
            mode="inline"
            selectedKeys={selected ? [selected] : []}
            items={items}
            onClick={({ key }) => navigate(String(key))}
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

function collectKeys(items: MenuProps['items']): string[] {
  if (!items) return [];
  const result: string[] = [];
  for (const item of items) {
    if (!item) continue;
    const anyItem = item as { key?: string; children?: MenuProps['items'] };
    if (anyItem.key && typeof anyItem.key === 'string' && anyItem.key.startsWith('/')) {
      result.push(anyItem.key);
    }
    if (anyItem.children) {
      result.push(...collectKeys(anyItem.children));
    }
  }
  return result;
}
