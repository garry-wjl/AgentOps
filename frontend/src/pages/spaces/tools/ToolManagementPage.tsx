import { PlusOutlined } from '@ant-design/icons';
import { Badge, Button, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { mockTools, type ToolItem, type ToolStatus, type ToolType } from '@/mock/tools';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<ToolStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

const HEALTH = {
  HEALTHY: { color: 'success', label: '健康' },
  DEGRADED: { color: 'warning', label: '降级' },
  UNKNOWN: { color: 'default', label: '未知' },
} as const;

export default function ToolManagementPage() {
  const [list, setList] = useState<ToolItem[]>(mockTools);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<ToolItem | null>(null);
  const [form] = Form.useForm();

  function openEdit(t: ToolItem) {
    setEditing(t);
    form.setFieldsValue(t);
    setDrawerOpen(true);
  }

  function openCreate() {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ type: 'MCP', authType: 'API_KEY' });
    setDrawerOpen(true);
  }

  function handleSubmit() {
    form.validateFields().then((v) => {
      if (editing) {
        setList((prev) => prev.map((it) => (it.id === editing.id ? { ...it, ...v } : it)));
      } else {
        setList((prev) => [
          {
            id: `tl-${Date.now()}`,
            num: `TL${Date.now()}001`,
            status: 'ENABLED',
            health: 'UNKNOWN',
            updatedBy: '当前用户',
            updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
            ...v,
          },
          ...prev,
        ]);
      }
      message.success('已保存');
      setDrawerOpen(false);
    });
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            工具管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            MCP 协议工具与 Function Call 工具的注册、测试
          </Paragraph>
        </div>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            注册工具
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        dataSource={list}
        pagination={{ pageSize: 10 }}
        columns={[
          {
            title: '名称',
            dataIndex: 'name',
            render: (v, r) => (
              <Space direction="vertical" size={0}>
                <Text strong>{v}</Text>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {r.num}
                </Text>
              </Space>
            ),
          },
          { title: 'Key', dataIndex: 'toolKey', render: (v) => <Text code>{v}</Text> },
          {
            title: '类型',
            dataIndex: 'type',
            width: 100,
            render: (t: ToolType) => <Tag color={t === 'MCP' ? 'geekblue' : 'gold'}>{t}</Tag>,
          },
          { title: 'Endpoint', dataIndex: 'endpoint', ellipsis: true },
          { title: '认证', dataIndex: 'authType', width: 80, render: (v) => <Tag>{v}</Tag> },
          {
            title: '健康',
            dataIndex: 'health',
            width: 90,
            render: (h: ToolItem['health']) => (
              <Badge status={HEALTH[h].color as any} text={HEALTH[h].label} />
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: ToolStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          {
            title: '操作',
            width: 160,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>查看</a>
                <a onClick={() => openEdit(r)}>编辑</a>
                <a>测试调用</a>
              </Space>
            ),
          },
        ]}
      />

      <Drawer
        title={editing ? `编辑工具 - ${editing.name}` : '注册工具'}
        width={520}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>
              保存
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="工具名称" rules={[{ required: true }]}>
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item name="toolKey" label="Key" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true }]}>
            <Select
              options={[
                { value: 'MCP', label: 'MCP 协议' },
                { value: 'FUNCTION_CALL', label: 'Function Call' },
              ]}
            />
          </Form.Item>
          <Form.Item name="endpoint" label="Endpoint">
            <Input placeholder="https://..." />
          </Form.Item>
          <Form.Item name="authType" label="认证方式">
            <Select
              options={[
                { value: 'NONE', label: '无' },
                { value: 'API_KEY', label: 'API Key' },
                { value: 'OAUTH', label: 'OAuth' },
              ]}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  );
}
