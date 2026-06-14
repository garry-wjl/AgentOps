import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import {
  Alert,
  Badge,
  Button,
  Drawer,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { useMemo, useState } from 'react';
import {
  mockSandboxes,
  type SandboxItem,
  type SandboxProvider,
  type SandboxStatus,
} from '@/mock/sandboxes';
import { ellipsisCell } from '@/utils/listCell';

const { Title, Paragraph, Text } = Typography;

const STATUS_BADGE: Record<SandboxStatus, { color: string; label: string; icon?: React.ReactNode }> = {
  DRAFT: { color: 'default', label: '草稿' },
  INITIALIZING: { color: 'processing', label: '初始化中', icon: <LoadingOutlined /> },
  ONLINE: { color: 'success', label: '在线' },
  OFFLINE: { color: 'warning', label: '离线' },
  DISABLED: { color: 'error', label: '禁用' },
};

const PROVIDER_LABEL: Record<SandboxProvider, string> = {
  OPEN_SANDBOX: 'OpenSandbox',
  ALIYUN_SANDBOX: '阿里云沙箱',
};

/**
 * 沙箱管理 —— 列表 + 右抽屉式查看/编辑/新建。
 */
export default function SandboxManagementPage() {
  const [list, setList] = useState<SandboxItem[]>(mockSandboxes);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<SandboxStatus | 'ALL'>('ALL');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerMode, setDrawerMode] = useState<'view' | 'edit' | 'create'>('create');
  const [active, setActive] = useState<SandboxItem | null>(null);
  const [form] = Form.useForm();

  const filtered = useMemo(
    () =>
      list.filter(
        (s) =>
          (statusFilter === 'ALL' || s.status === statusFilter) &&
          (!keyword ||
            s.name.includes(keyword) ||
            s.num.includes(keyword) ||
            (s.remark || '').includes(keyword)),
      ),
    [list, keyword, statusFilter],
  );

  function openCreate() {
    setActive(null);
    setDrawerMode('create');
    form.resetFields();
    form.setFieldsValue({
      type: 'CODE',
      provider: 'OPEN_SANDBOX',
      cpu: 1,
      memoryMb: 2048,
      ttlMinutes: 60,
      envVars: [],
    });
    setDrawerOpen(true);
  }

  function openView(s: SandboxItem) {
    setActive(s);
    setDrawerMode('view');
    form.setFieldsValue(s);
    setDrawerOpen(true);
  }

  function openEdit(s: SandboxItem) {
    setActive(s);
    setDrawerMode('edit');
    form.setFieldsValue(s);
    setDrawerOpen(true);
  }

  function disable(s: SandboxItem) {
    setList((prev) =>
      prev.map((it) =>
        it.id === s.id
          ? { ...it, status: 'DISABLED' as const, lastTransitionReason: '人工禁用' }
          : it,
      ),
    );
    message.success(`已禁用 ${s.name}`);
  }

  function enable(s: SandboxItem) {
    setList((prev) =>
      prev.map((it) =>
        it.id === s.id
          ? { ...it, status: 'INITIALIZING' as const, lastTransitionReason: '人工启用 - 重新初始化' }
          : it,
      ),
    );
    message.success(`${s.name} 进入初始化`);
  }

  function remove(s: SandboxItem) {
    setList((prev) => prev.filter((it) => it.id !== s.id));
    message.success('已删除草稿沙箱');
  }

  function handleSubmit(submit: boolean) {
    form.validateFields().then((values) => {
      if (drawerMode === 'create') {
        const it: SandboxItem = {
          id: `sb-${Date.now()}`,
          num: submit ? `SB${Date.now()}001` : '',
          envVars: values.envVars || [],
          status: submit ? 'INITIALIZING' : 'DRAFT',
          updatedBy: '当前用户',
          updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
          ...values,
        };
        setList((prev) => [it, ...prev]);
        message.success(submit ? '已提交，正在初始化' : '草稿已保存');
      } else if (active) {
        setList((prev) =>
          prev.map((it) =>
            it.id === active.id
              ? {
                  ...it,
                  ...values,
                  envVars: values.envVars || [],
                  updatedBy: '当前用户',
                  updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
                }
              : it,
          ),
        );
        message.success('已保存');
      }
      setDrawerOpen(false);
    });
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            沙箱管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            远程代码沙箱实例 · 草稿 → 初始化中 → 在线 ↔ 离线（系统驱动） · 禁用为人工操作
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索名称/编码/备注"
            allowClear
            onSearch={setKeyword}
            style={{ width: 260 }}
          />
          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            style={{ width: 120 }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'INITIALIZING', label: '初始化中' },
              { value: 'ONLINE', label: '在线' },
              { value: 'OFFLINE', label: '离线' },
              { value: 'DISABLED', label: '禁用' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建沙箱
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        dataSource={filtered}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1240 }}
        tableLayout="fixed"
        columns={[
          {
            title: '编码',
            dataIndex: 'num',
            width: 240,
            fixed: 'left',
            render: (v: string) => <Text code>{v || '草稿未生成'}</Text>,
          },
          {
            title: '名称',
            dataIndex: 'name',
            width: 200,
            render: (v: string) => <Text strong>{v}</Text>,
          },
          {
            title: '供应商',
            dataIndex: 'provider',
            width: 130,
            render: (p: SandboxProvider) => <Tag color="geekblue">{PROVIDER_LABEL[p]}</Tag>,
          },
          {
            title: '资源',
            width: 140,
            render: (_, r) => `${r.cpu}核 / ${r.memoryMb}MB`,
          },
          {
            title: '存活',
            dataIndex: 'ttlMinutes',
            width: 80,
            render: (v) => `${v}分钟`,
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 140,
            render: (s: SandboxStatus, r) => {
              const it = STATUS_BADGE[s];
              return (
                <Space direction="vertical" size={0}>
                  <Badge
                    status={it.color as any}
                    text={
                      <Space size={4}>
                        {it.icon}
                        {it.label}
                      </Space>
                    }
                  />
                  {r.lastTransitionReason && (
                    <Text type="secondary" style={{ fontSize: 11 }}>
                      {ellipsisCell(r.lastTransitionReason, 16)}
                    </Text>
                  )}
                </Space>
              );
            },
          },
          { title: '最近探活', dataIndex: 'lastProbeAt', width: 160 },
          {
            title: '操作',
            width: 220,
            fixed: 'right',
            render: (_, r) => (
              <Space>
                <a onClick={() => openView(r)}>查看</a>
                {r.status !== 'DISABLED' && r.status !== 'DRAFT' && (
                  <a onClick={() => openEdit(r)}>编辑</a>
                )}
                {r.status === 'DRAFT' && <a onClick={() => openEdit(r)}>编辑</a>}
                {r.status === 'DRAFT' && (
                  <Popconfirm title="确定删除该草稿沙箱？" onConfirm={() => remove(r)}>
                    <a style={{ color: '#ff4d4f' }}>删除</a>
                  </Popconfirm>
                )}
                {r.status !== 'DISABLED' && r.status !== 'DRAFT' && (
                  <Popconfirm
                    title={
                      <>
                        禁用 <b>{r.name}</b>？已引用该沙箱的 Agent 将无法执行代码
                      </>
                    }
                    onConfirm={() => disable(r)}
                  >
                    <a style={{ color: '#ff4d4f' }}>禁用</a>
                  </Popconfirm>
                )}
                {r.status === 'DISABLED' && (
                  <Popconfirm
                    title={`启用 ${r.name}？将重新走初始化流程`}
                    onConfirm={() => enable(r)}
                  >
                    <a>启用</a>
                  </Popconfirm>
                )}
              </Space>
            ),
          },
        ]}
      />

      <Drawer
        title={
          drawerMode === 'create'
            ? '新建沙箱'
            : drawerMode === 'edit'
              ? `编辑沙箱 - ${active?.name}`
              : `沙箱详情 - ${active?.name}`
        }
        width={560}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={
          drawerMode === 'view' ? (
            <Space>
              {active?.status !== 'DRAFT' && active?.status !== 'DISABLED' && (
                <Button danger onClick={() => active && disable(active)}>
                  禁用
                </Button>
              )}
              {active?.status === 'DISABLED' && (
                <Button type="primary" onClick={() => active && enable(active)}>
                  启用
                </Button>
              )}
              {active && active.status !== 'DRAFT' && (
                <Button onClick={() => setDrawerMode('edit')}>编辑</Button>
              )}
              <Button onClick={() => setDrawerOpen(false)}>关闭</Button>
            </Space>
          ) : (
            <Space>
              <Button onClick={() => setDrawerOpen(false)}>取消</Button>
              {drawerMode === 'create' && (
                <>
                  <Button onClick={() => handleSubmit(false)}>保存为草稿</Button>
                  <Button type="primary" onClick={() => handleSubmit(true)}>
                    保存并提交
                  </Button>
                </>
              )}
              {drawerMode === 'edit' && (
                <Button type="primary" onClick={() => handleSubmit(false)}>
                  保存
                </Button>
              )}
            </Space>
          )
        }
      >
        {drawerMode !== 'create' && active?.status !== 'DRAFT' && drawerMode === 'edit' && (
          <Alert
            type="warning"
            showIcon
            message="环境变量已变更需禁用后再启用方能生效；CPU/内存/存活时间/类型/供应商在提交后不可修改"
            style={{ marginBottom: 12 }}
          />
        )}
        <Form form={form} layout="vertical" disabled={drawerMode === 'view'}>
          <Form.Item label="业务编码">
            <Input value={active?.num || '系统提交后生成'} disabled />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item name="type" label="类型">
            <Select
              disabled={drawerMode === 'edit' && active?.status !== 'DRAFT'}
              options={[{ value: 'CODE', label: '代码沙箱' }]}
            />
          </Form.Item>
          <Form.Item name="provider" label="供应商">
            <Select
              disabled={drawerMode === 'edit' && active?.status !== 'DRAFT'}
              options={[
                { value: 'OPEN_SANDBOX', label: 'OpenSandbox' },
                { value: 'ALIYUN_SANDBOX', label: '阿里云沙箱' },
              ]}
            />
          </Form.Item>
          <Space size={16} wrap>
            <Form.Item name="cpu" label="CPU（核）">
              <InputNumber
                min={0.5}
                max={16}
                step={0.5}
                disabled={drawerMode === 'edit' && active?.status !== 'DRAFT'}
              />
            </Form.Item>
            <Form.Item name="memoryMb" label="内存（MB）">
              <InputNumber
                min={128}
                max={32768}
                step={128}
                disabled={drawerMode === 'edit' && active?.status !== 'DRAFT'}
              />
            </Form.Item>
            <Form.Item name="ttlMinutes" label="存活时间（分钟）">
              <InputNumber
                min={1}
                max={1440}
                disabled={drawerMode === 'edit' && active?.status !== 'DRAFT'}
              />
            </Form.Item>
          </Space>
          <Form.Item name="endpointOverride" label="接入地址覆盖（可选）">
            <Input placeholder="留空使用系统设置默认" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} />
          </Form.Item>
          <Form.List name="envVars">
            {(fields, { add, remove }) => (
              <div>
                <div className="form-list-head">
                  <span>环境变量（最多 50 项）</span>
                  {drawerMode !== 'view' && <a onClick={() => add({ key: '', value: '', remark: '' })}>+ 新增一行</a>}
                </div>
                {fields.map((field) => (
                  <Space key={field.key} align="baseline" style={{ display: 'flex' }}>
                    <Form.Item name={[field.name, 'key']} rules={[{ required: true, message: 'key 必填' }]}>
                      <Input placeholder="KEY" style={{ width: 160 }} />
                    </Form.Item>
                    <Form.Item name={[field.name, 'value']} rules={[{ required: true }]}>
                      <Input.Password placeholder="VALUE" style={{ width: 200 }} />
                    </Form.Item>
                    <Form.Item name={[field.name, 'remark']}>
                      <Input placeholder="备注" style={{ width: 140 }} />
                    </Form.Item>
                    {drawerMode !== 'view' && <a onClick={() => remove(field.name)}>移除</a>}
                  </Space>
                ))}
              </div>
            )}
          </Form.List>
          {drawerMode === 'view' && active && (
            <>
              <Form.Item label="远程实例标识">
                <Input value={active.remoteInstanceId || '-'} disabled />
              </Form.Item>
              <Form.Item label="最近探活时间">
                <Input value={active.lastProbeAt || '-'} disabled />
              </Form.Item>
              <Form.Item label="最近变更原因">
                <Input value={active.lastTransitionReason || '-'} disabled />
              </Form.Item>
            </>
          )}
        </Form>
      </Drawer>
    </div>
  );
}
