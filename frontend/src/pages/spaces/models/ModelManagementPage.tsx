import { PlusOutlined } from '@ant-design/icons';
import { Badge, Button, Drawer, Form, Input, InputNumber, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { mockModels, type ModelItem, type ModelStatus } from '@/mock/models';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<ModelStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

const HEALTH: Record<ModelItem['health'], { color: string; label: string }> = {
  HEALTHY: { color: 'success', label: '健康' },
  DEGRADED: { color: 'warning', label: '降级' },
  UNKNOWN: { color: 'default', label: '未知' },
};

const PROVIDERS: { value: ModelItem['provider']; label: string }[] = [
  { value: 'OPENAI', label: 'OpenAI' },
  { value: 'ANTHROPIC', label: 'Anthropic' },
  { value: 'AZURE', label: 'Azure OpenAI' },
  { value: 'OLLAMA', label: 'Ollama 本地' },
  { value: 'CUSTOM', label: '自定义' },
];

export default function ModelManagementPage() {
  const [list, setList] = useState<ModelItem[]>(mockModels);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<ModelItem | null>(null);
  const [form] = Form.useForm();

  function openCreate() {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ presetTemperature: 0.7, presetMaxTokens: 4096 });
    setDrawerOpen(true);
  }

  function openEdit(m: ModelItem) {
    setEditing(m);
    form.setFieldsValue(m);
    setDrawerOpen(true);
  }

  function handleSubmit() {
    form.validateFields().then((v) => {
      if (editing) {
        setList((prev) => prev.map((it) => (it.id === editing.id ? { ...it, ...v } : it)));
      } else {
        const m: ModelItem = {
          id: `mo-${Date.now()}`,
          num: `MO${Date.now()}001`,
          status: 'ENABLED',
          health: 'UNKNOWN',
          updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
          hasApiKey: true,
          ...v,
        };
        setList((prev) => [m, ...prev]);
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
            模型管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 LLM 模型供应商配置、API Key 与参数预设
          </Paragraph>
        </div>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建模型
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
          {
            title: '供应商',
            dataIndex: 'provider',
            render: (p) => <Tag color="purple">{PROVIDERS.find((x) => x.value === p)?.label}</Tag>,
          },
          { title: 'Model ID', dataIndex: 'modelId', width: 180 },
          { title: 'Endpoint', dataIndex: 'endpoint' },
          {
            title: 'API Key',
            dataIndex: 'hasApiKey',
            width: 100,
            render: (v) => (v ? <Tag color="blue">已配置</Tag> : <Tag>未配置</Tag>),
          },
          {
            title: '预设参数',
            width: 160,
            render: (_, r) => `T=${r.presetTemperature} / max=${r.presetMaxTokens}`,
          },
          {
            title: '健康',
            dataIndex: 'health',
            width: 90,
            render: (h: ModelItem['health']) => (
              <Badge status={HEALTH[h].color as any} text={HEALTH[h].label} />
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: ModelStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          {
            title: '操作',
            width: 160,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>编辑</a>
                <a>测试连通性</a>
              </Space>
            ),
          },
        ]}
      />

      <Drawer
        title={editing ? `编辑模型 - ${editing.name}` : '新建模型'}
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
          <Form.Item name="name" label="模型名称" rules={[{ required: true }]}>
            <Input maxLength={50} placeholder="例如：OpenAI GPT-4o" />
          </Form.Item>
          <Form.Item name="provider" label="供应商" rules={[{ required: true }]}>
            <Select options={PROVIDERS} />
          </Form.Item>
          <Form.Item name="modelId" label="Model ID" rules={[{ required: true }]}>
            <Input placeholder="例如：gpt-4o" />
          </Form.Item>
          <Form.Item name="endpoint" label="Endpoint">
            <Input placeholder="https://api.openai.com/v1" />
          </Form.Item>
          <Form.Item label="API Key">
            <Input.Password placeholder={editing?.hasApiKey ? '****（已配置）' : '请输入'} />
          </Form.Item>
          <Space>
            <Form.Item name="presetTemperature" label="Temperature">
              <InputNumber min={0} max={2} step={0.1} />
            </Form.Item>
            <Form.Item name="presetMaxTokens" label="Max Tokens">
              <InputNumber min={256} max={32768} step={256} />
            </Form.Item>
          </Space>
        </Form>
      </Drawer>
    </div>
  );
}
