import {
  CrownOutlined,
  DeleteOutlined,
  EditOutlined,
  EllipsisOutlined,
  PlusOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Drawer,
  Dropdown,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import type { MenuProps } from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { mockSpaces, type Space as SpaceItem, type SpaceMember } from '@/mock/spaces';
import { mockPlatformUsers } from '@/mock/users';

const { Title, Paragraph, Text } = Typography;

/**
 * 当前登录用户的占位 userNum —— Mock 数据下视为「创建人」，不可被移出管理员名单。
 */
const CURRENT_USER_NUM = 'US-ME';

/**
 * 空间卡片列表（平台 Shell 默认落地页，对应《空间管理 PRD》§8.1）。
 * 使用本地 state + Mock 数据，刷新后回到初始数据。
 */
export default function SpaceListPage() {
  const navigate = useNavigate();
  const [spaces, setSpaces] = useState<SpaceItem[]>(mockSpaces);
  const [keyword, setKeyword] = useState('');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingSpace, setEditingSpace] = useState<SpaceItem | null>(null);
  const [deleting, setDeleting] = useState<SpaceItem | null>(null);
  const [confirmText, setConfirmText] = useState('');
  const [form] = Form.useForm();
  // 编辑/新建抽屉中的成员选择（与表单分开管理，便于互斥逻辑）
  const [adminUserNums, setAdminUserNums] = useState<string[]>([CURRENT_USER_NUM]);
  const [memberUserNums, setMemberUserNums] = useState<string[]>([]);

  const filtered = useMemo(
    () => spaces.filter((s) => !keyword || s.name.includes(keyword) || s.num.includes(keyword)),
    [spaces, keyword],
  );

  const expectedConfirm = deleting ? `我确定删除${deleting.name}空间` : '';
  const canDelete = confirmText === expectedConfirm;

  function openCreate() {
    setEditingSpace(null);
    form.resetFields();
    setAdminUserNums([CURRENT_USER_NUM]);
    setMemberUserNums([]);
    setDrawerOpen(true);
  }

  function openEdit(space: SpaceItem) {
    setEditingSpace(space);
    form.setFieldsValue({
      name: space.name,
      remark: space.remark,
    });
    setAdminUserNums(space.admins.map((m) => m.userNum));
    setMemberUserNums(space.members.map((m) => m.userNum));
    setDrawerOpen(true);
  }

  /**
   * 把 userNum 列表映射为 SpaceMember[]。
   * 如果原空间已有该用户的 SpaceMember，沿用其 role；否则按 fallback 角色生成。
   */
  function toMembers(
    userNums: string[],
    fallbackRole: SpaceMember['role'],
    base?: SpaceMember[],
  ): SpaceMember[] {
    return userNums.map((num) => {
      const existing = base?.find((m) => m.userNum === num);
      if (existing) return { ...existing, role: fallbackRole };
      const opt = mockPlatformUsers.find((u) => u.userNum === num);
      return {
        userNum: num,
        name: opt?.name ?? num,
        email: opt?.email ?? '',
        role: fallbackRole,
      };
    });
  }

  function handleSubmit() {
    form.validateFields().then((values) => {
      // 创建人始终是 OWNER，其余管理员 ADMIN
      const adminMembers: SpaceMember[] = adminUserNums.map((num) => {
        const original =
          editingSpace?.admins.find((m) => m.userNum === num) ??
          editingSpace?.members.find((m) => m.userNum === num);
        const opt = mockPlatformUsers.find((u) => u.userNum === num);
        return {
          userNum: num,
          name: original?.name ?? opt?.name ?? num,
          email: original?.email ?? opt?.email ?? '',
          role: num === CURRENT_USER_NUM ? 'OWNER' : 'ADMIN',
        };
      });
      const memberMembers = toMembers(memberUserNums, 'MEMBER', [
        ...(editingSpace?.admins ?? []),
        ...(editingSpace?.members ?? []),
      ]);

      if (editingSpace) {
        setSpaces((prev) =>
          prev.map((s) =>
            s.id === editingSpace.id
              ? { ...s, ...values, admins: adminMembers, members: memberMembers }
              : s,
          ),
        );
        message.success('已更新空间信息');
      } else {
        const newSpace: SpaceItem = {
          id: `sp-${Date.now()}`,
          num: `SP${Date.now()}001`,
          name: values.name,
          remark: values.remark,
          createdBy: '当前用户',
          createdAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
          myRole: 'ADMIN',
          admins: adminMembers,
          members: memberMembers,
        };
        setSpaces((prev) => [newSpace, ...prev]);
        message.success('空间创建成功');
      }
      setDrawerOpen(false);
    });
  }

  function handleDelete() {
    if (!deleting) return;
    setSpaces((prev) => prev.filter((s) => s.id !== deleting.id));
    message.success(`空间 ${deleting.name} 已删除`);
    setDeleting(null);
    setConfirmText('');
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            空间管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            登录后默认落地页 · 卡片式浏览你参与的全部空间，点击「进入」切换至空间内
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索空间名称或编码"
            allowClear
            onSearch={setKeyword}
            style={{ width: 260 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建空间
          </Button>
        </Space>
      </div>

      <div className="space-card-grid">
        <button type="button" className="space-card space-card-create" onClick={openCreate}>
          <PlusOutlined />
          <div>新建空间</div>
        </button>
        {filtered.map((s) => {
          const adminMenu: MenuProps['items'] =
            s.myRole === 'ADMIN'
              ? [
                  {
                    key: 'edit',
                    icon: <EditOutlined />,
                    label: '编辑',
                  },
                  {
                    key: 'delete',
                    icon: <DeleteOutlined />,
                    label: '删除',
                    danger: true,
                  },
                ]
              : [];

          return (
            <Card
              key={s.id}
              className="space-card space-card-clickable"
              hoverable
              variant="outlined"
              onClick={() => navigate(`/spaces/${s.id}/agents`)}
            >
              <div className="space-card-head">
                <span className="space-card-icon">
                  <CrownOutlined />
                </span>
                <div className="space-card-title">
                  <Text strong>{s.name}</Text>
                  <Tag color={s.myRole === 'ADMIN' ? 'blue' : 'green'}>
                    {s.myRole === 'ADMIN' ? '管理' : '成员'}
                  </Tag>
                </div>
                {adminMenu.length > 0 && (
                  <Dropdown
                    trigger={['hover', 'click']}
                    placement="bottomRight"
                    menu={{
                      items: adminMenu,
                      onClick: ({ key, domEvent }) => {
                        domEvent.stopPropagation();
                        if (key === 'edit') {
                          openEdit(s);
                        } else if (key === 'delete') {
                          setDeleting(s);
                          setConfirmText('');
                        }
                      },
                    }}
                  >
                    <button
                      type="button"
                      className="space-card-more"
                      onClick={(e) => e.stopPropagation()}
                      aria-label="更多操作"
                    >
                      <EllipsisOutlined />
                    </button>
                  </Dropdown>
                )}
              </div>
              <Paragraph type="secondary" className="space-card-num">
                {s.num}
              </Paragraph>
              <div className="space-card-meta">
                <Tooltip title="创建人">
                  <Space size={4}>
                    <UserOutlined /> {s.createdBy}
                  </Space>
                </Tooltip>
                <Tooltip title="成员">
                  <Space size={4}>
                    <TeamOutlined /> {s.admins.length} 管理 / {s.members.length} 成员
                  </Space>
                </Tooltip>
              </div>
              <Paragraph
                ellipsis={{ rows: 1 }}
                className="space-card-remark"
                type="secondary"
              >
                {s.remark || '—'}
              </Paragraph>
            </Card>
          );
        })}
      </div>

      <Drawer
        title={editingSpace ? `编辑空间 - ${editingSpace.name}` : '新建空间'}
        width={520}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              {editingSpace ? '保存' : '确定创建'}
            </Button>
          </Space>
        }
      >
        <Form layout="vertical" form={form}>
          <Form.Item label="业务编码">
            <Input value={editingSpace?.num || '系统提交后生成'} disabled />
          </Form.Item>
          <Form.Item
            name="name"
            label="空间名称"
            rules={[{ required: true, message: '请输入空间名称' }]}
          >
            <Input maxLength={30} placeholder="1～30 字符" showCount />
          </Form.Item>
          <Form.Item label="管理员" required>
            <Select
              mode="multiple"
              value={adminUserNums}
              onChange={(next: string[]) => {
                // 创建人不可被移出
                if (!next.includes(CURRENT_USER_NUM)) {
                  next = [CURRENT_USER_NUM, ...next];
                }
                setAdminUserNums(next);
                // 管理员中已选的用户不能同时出现在成员里
                setMemberUserNums((prev) => prev.filter((u) => !next.includes(u)));
              }}
              optionLabelProp="label"
              placeholder="选择平台用户"
              style={{ width: '100%' }}
              options={mockPlatformUsers.map((u) => ({
                value: u.userNum,
                label:
                  u.userNum === CURRENT_USER_NUM
                    ? `${u.name}（创建人，不可移除）`
                    : `${u.name} · ${u.email}`,
                disabled: u.userNum === CURRENT_USER_NUM, // 创建人在多选中不可被取消
              }))}
              tagRender={(props) => {
                const isOwner = props.value === CURRENT_USER_NUM;
                return (
                  <Tag
                    color={isOwner ? 'gold' : 'blue'}
                    closable={!isOwner && props.closable}
                    onClose={props.onClose}
                    icon={isOwner ? <CrownOutlined /> : <UserOutlined />}
                    style={{ marginInlineEnd: 4 }}
                  >
                    {props.label}
                  </Tag>
                );
              }}
            />
            <div className="form-hint">
              选项来自《用户管理》中状态为「启用」的用户；创建人始终为管理员且不可移除
            </div>
          </Form.Item>
          <Form.Item label="普通成员">
            <Select
              mode="multiple"
              value={memberUserNums}
              onChange={(next: string[]) => {
                // 兜底过滤：不允许已是管理员的人出现在成员里
                setMemberUserNums(next.filter((u) => !adminUserNums.includes(u)));
              }}
              placeholder="选择平台用户"
              style={{ width: '100%' }}
              options={mockPlatformUsers.map((u) => ({
                value: u.userNum,
                label: `${u.name} · ${u.email}`,
                disabled: adminUserNums.includes(u.userNum),
              }))}
            />
            <div className="form-hint">
              管理员名单中已选择的用户在普通成员中将被置灰
            </div>
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} maxLength={200} showCount />
          </Form.Item>
        </Form>
      </Drawer>

      <Modal
        title={
          <span>
            <span className="modal-warning-icon">⚠</span> 删除空间
          </span>
        }
        open={!!deleting}
        onCancel={() => {
          setDeleting(null);
          setConfirmText('');
        }}
        okText="确定删除"
        cancelText="取消"
        okButtonProps={{ danger: true, disabled: !canDelete }}
        onOk={handleDelete}
      >
        {deleting && (
          <>
            <Alert
              type="warning"
              showIcon
              message="空间删除以后，空间里面的内容将不再可用。"
              style={{ marginBottom: 12 }}
            />
            <Paragraph>
              请输入 <Text code>{expectedConfirm}</Text> 以确认操作：
            </Paragraph>
            <Input
              value={confirmText}
              onChange={(e) => setConfirmText(e.target.value)}
              placeholder={expectedConfirm}
            />
          </>
        )}
      </Modal>
    </div>
  );
}
