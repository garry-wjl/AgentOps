import { CrownOutlined, DeleteOutlined, PlusOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Button, Drawer, Input, Space, Table, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { findSpace, mockSpaces, type SpaceMember, type SpaceRole } from '@/mock/spaces';

const { Title, Paragraph, Text } = Typography;

const ROLE_LABEL: Record<SpaceRole, { color: string; label: string; icon?: React.ReactNode }> = {
  OWNER: { color: 'gold', label: '创建人', icon: <CrownOutlined /> },
  ADMIN: { color: 'blue', label: '管理员' },
  MEMBER: { color: 'green', label: '普通成员' },
};

/**
 * 空间成员（仅空间管理员可见）。
 */
export default function SpaceMembersPage() {
  const { spaceId = '' } = useParams();
  const initial = findSpace(spaceId) ?? mockSpaces[0];
  const [members, setMembers] = useState<SpaceMember[]>([
    ...initial.admins,
    ...initial.members,
  ]);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [searchEmail, setSearchEmail] = useState('');

  function removeMember(m: SpaceMember) {
    if (m.role === 'OWNER') {
      message.warning('创建人不可被移除');
      return;
    }
    setMembers((prev) => prev.filter((x) => x.userNum !== m.userNum));
    message.success(`已移除 ${m.name}`);
  }

  function changeRole(m: SpaceMember, role: SpaceRole) {
    if (m.role === 'OWNER') {
      message.warning('创建人角色不可变更');
      return;
    }
    setMembers((prev) =>
      prev.map((x) => (x.userNum === m.userNum ? { ...x, role } : x)),
    );
    message.success('角色已更新');
  }

  function inviteMember() {
    if (!searchEmail) return;
    const newMember: SpaceMember = {
      userNum: `US-${Date.now()}`,
      name: searchEmail.split('@')[0],
      email: searchEmail,
      role: 'MEMBER',
    };
    setMembers((prev) => [...prev, newMember]);
    message.success(`已邀请 ${searchEmail}`);
    setSearchEmail('');
    setDrawerOpen(false);
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            空间成员
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            管理空间内的管理员与普通成员（仅管理员可见此入口）
          </Paragraph>
        </div>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setDrawerOpen(true)}>
            邀请成员
          </Button>
        </Space>
      </div>

      <Table
        rowKey="userNum"
        dataSource={members}
        pagination={false}
        columns={[
          {
            title: '成员',
            render: (_, r) => (
              <Space>
                <Avatar icon={<UserOutlined />} />
                <Space direction="vertical" size={0}>
                  <Text strong>{r.name}</Text>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {r.email}
                  </Text>
                </Space>
              </Space>
            ),
          },
          { title: '用户编码', dataIndex: 'userNum', width: 180 },
          {
            title: '角色',
            dataIndex: 'role',
            width: 200,
            render: (role: SpaceRole, r) => (
              <Space>
                <Tag color={ROLE_LABEL[role].color} icon={ROLE_LABEL[role].icon}>
                  {ROLE_LABEL[role].label}
                </Tag>
                {role !== 'OWNER' && (
                  <a
                    onClick={() =>
                      changeRole(r, role === 'ADMIN' ? 'MEMBER' : 'ADMIN')
                    }
                  >
                    {role === 'ADMIN' ? '设为普通成员' : '设为管理员'}
                  </a>
                )}
              </Space>
            ),
          },
          {
            title: '操作',
            width: 120,
            render: (_, r) =>
              r.role === 'OWNER' ? (
                <Text type="secondary">不可移除</Text>
              ) : (
                <a onClick={() => removeMember(r)} style={{ color: '#ff4d4f' }}>
                  <DeleteOutlined /> 移除
                </a>
              ),
          },
        ]}
      />

      <Drawer
        title="邀请成员"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        width={420}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={inviteMember}>
              发送邀请
            </Button>
          </Space>
        }
      >
        <p style={{ color: '#666' }}>
          通过用户邮箱搜索平台中已启用的用户。被邀请的用户将默认作为「普通成员」加入空间，加入后管理员可调整为「管理员」。
        </p>
        <Input
          placeholder="输入用户邮箱"
          value={searchEmail}
          onChange={(e) => setSearchEmail(e.target.value)}
        />
      </Drawer>
    </div>
  );
}
