import {
  CrownOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Avatar,
  Button,
  Card,
  Drawer,
  Form,
  Input,
  Modal,
  Space,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { mockSpaces, type Space as SpaceItem } from '@/mock/spaces';

const { Title, Paragraph, Text } = Typography;

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

  const filtered = useMemo(
    () => spaces.filter((s) => !keyword || s.name.includes(keyword) || s.num.includes(keyword)),
    [spaces, keyword],
  );

  const expectedConfirm = deleting ? `我确定删除${deleting.name}空间` : '';
  const canDelete = confirmText === expectedConfirm;

  function openCreate() {
    setEditingSpace(null);
    form.resetFields();
    setDrawerOpen(true);
  }

  function openEdit(space: SpaceItem) {
    setEditingSpace(space);
    form.setFieldsValue({
      name: space.name,
      remark: space.remark,
    });
    setDrawerOpen(true);
  }

  function handleSubmit() {
    form.validateFields().then((values) => {
      if (editingSpace) {
        setSpaces((prev) =>
          prev.map((s) => (s.id === editingSpace.id ? { ...s, ...values } : s)),
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
          admins: [
            { userNum: 'US-ME', name: '当前用户', email: 'me@example.com', role: 'OWNER' },
          ],
          members: [],
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
        {filtered.map((s) => (
          <Card key={s.id} className="space-card" hoverable variant="outlined">
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
              ellipsis={{ rows: 2 }}
              className="space-card-remark"
              type="secondary"
            >
              {s.remark || '—'}
            </Paragraph>
            <div className="space-card-avatars">
              {[...s.admins, ...s.members].slice(0, 5).map((m) => (
                <Tooltip key={m.userNum} title={`${m.name} (${m.email})`}>
                  <Avatar size={28} icon={<UserOutlined />} />
                </Tooltip>
              ))}
              {s.admins.length + s.members.length > 5 && (
                <Avatar size={28} className="space-card-avatars-more">
                  +{s.admins.length + s.members.length - 5}
                </Avatar>
              )}
            </div>
            <div className="space-card-footer">
              <Button
                type="primary"
                onClick={() => navigate(`/spaces/${s.id}/dashboard`)}
              >
                进入空间
              </Button>
              {s.myRole === 'ADMIN' && (
                <>
                  <Button icon={<EditOutlined />} onClick={() => openEdit(s)}>
                    编辑
                  </Button>
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    onClick={() => {
                      setDeleting(s);
                      setConfirmText('');
                    }}
                  >
                    删除
                  </Button>
                </>
              )}
            </div>
          </Card>
        ))}
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
          <Form.Item label="管理员">
            <Tag icon={<UserOutlined />} color="blue">
              当前用户（创建人，不可移除）
            </Tag>
            <Tag>+ 选择更多</Tag>
            <div className="form-hint">
              选项来自《用户管理》中状态为「启用」的用户，可多选
            </div>
          </Form.Item>
          <Form.Item label="普通成员">
            <Input placeholder="选择用户（暂为占位输入）" disabled />
            <div className="form-hint">
              管理员名单中已存在的用户在普通成员中将被置灰
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
