import { PlusOutlined } from '@ant-design/icons';
import { Button, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { mockAgents, type AgentItem, type AgentStatus } from '@/mock/agents';
import { mockModels } from '@/mock/models';
import { mockPrompts } from '@/mock/prompts';
import { mockSkills } from '@/mock/skills';
import { mockTools } from '@/mock/tools';
import { mockSandboxes } from '@/mock/sandboxes';

const { Title, Paragraph, Text } = Typography;

const STATUS_LABELS: Record<AgentStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ONLINE: { color: 'green', label: '在线' },
  OFFLINE: { color: 'red', label: '离线' },
};

export default function AgentManagementPage() {
  const [agents, setAgents] = useState<AgentItem[]>(mockAgents);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<AgentStatus | 'ALL'>('ALL');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<AgentItem | null>(null);
  const [form] = Form.useForm();

  const filtered = useMemo(
    () =>
      agents.filter(
        (a) =>
          (statusFilter === 'ALL' || a.status === statusFilter) &&
          (!keyword || a.name.includes(keyword) || a.num.includes(keyword)),
      ),
    [agents, statusFilter, keyword],
  );

  function openCreate() {
    setEditing(null);
    form.resetFields();
    setDrawerOpen(true);
  }

  function openEdit(a: AgentItem) {
    setEditing(a);
    form.setFieldsValue({
      name: a.name,
      description: a.description,
      modelName: a.modelName,
      promptKey: a.promptKey,
      skills: a.skills,
      tools: a.tools,
      sandboxName: a.sandboxName,
    });
    setDrawerOpen(true);
  }

  function handleSubmit() {
    form.validateFields().then((values) => {
      if (editing) {
        setAgents((prev) => prev.map((a) => (a.id === editing.id ? { ...a, ...values } : a)));
        message.success('Agent 已更新');
      } else {
        const a: AgentItem = {
          id: `ag-${Date.now()}`,
          num: `AG${Date.now()}001`,
          status: 'DRAFT',
          updatedBy: '当前用户',
          updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
          ...values,
        };
        setAgents((prev) => [a, ...prev]);
        message.success('Agent 创建成功');
      }
      setDrawerOpen(false);
    });
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Agent 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 Agent 的创建、配置、上下线
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索 Agent 名称或编码"
            allowClear
            onSearch={setKeyword}
            style={{ width: 240 }}
          />
          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            style={{ width: 120 }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'ONLINE', label: '在线' },
              { value: 'OFFLINE', label: '离线' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建 Agent
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        dataSource={filtered}
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
            title: '模型',
            dataIndex: 'modelName',
            width: 160,
            render: (v) => <Tag color="purple">{v}</Tag>,
          },
          { title: 'Prompt Key', dataIndex: 'promptKey', width: 220 },
          {
            title: 'Skill / 工具 / 沙箱',
            render: (_, r) => (
              <Space wrap size={4}>
                {r.skills.map((s) => (
                  <Tag key={s} color="cyan">
                    {s}
                  </Tag>
                ))}
                {r.tools.map((s) => (
                  <Tag key={s} color="gold">
                    {s}
                  </Tag>
                ))}
                {r.sandboxName && <Tag color="blue">沙箱:{r.sandboxName}</Tag>}
              </Space>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: AgentStatus) => (
              <Tag color={STATUS_LABELS[s].color}>{STATUS_LABELS[s].label}</Tag>
            ),
          },
          { title: '最近修改', dataIndex: 'updatedBy', width: 100 },
          { title: '更新时间', dataIndex: 'updatedAt', width: 140 },
          {
            title: '操作',
            width: 200,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>查看</a>
                <a onClick={() => openEdit(r)}>编辑</a>
                {r.status === 'DRAFT' && <a>提交</a>}
                {r.status === 'ONLINE' && <a>下线</a>}
                {r.status === 'OFFLINE' && <a>上线</a>}
              </Space>
            ),
          },
        ]}
      />

      <Drawer
        title={editing ? `编辑 Agent - ${editing.name}` : '新建 Agent'}
        width={640}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button onClick={handleSubmit}>保存为草稿</Button>
            <Button type="primary" onClick={handleSubmit}>
              保存并提交
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item label="业务编码">
            <Input value={editing?.num || '系统提交后生成'} disabled />
          </Form.Item>
          <Form.Item name="name" label="Agent 名称" rules={[{ required: true }]}>
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} maxLength={200} />
          </Form.Item>
          <Form.Item name="modelName" label="模型" rules={[{ required: true }]}>
            <Select
              placeholder="选择模型"
              options={mockModels.filter((m) => m.status === 'ENABLED').map((m) => ({
                value: m.name,
                label: `${m.name} · ${m.modelId}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="promptKey" label="System Prompt（按 Key 引用）" rules={[{ required: true }]}>
            <Select
              showSearch
              placeholder="选择启用态 Prompt"
              options={mockPrompts.filter((p) => p.status === 'ENABLED').map((p) => ({
                value: p.promptKey,
                label: `${p.promptKey} · ${p.name}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="skills" label="绑定 Skill">
            <Select
              mode="multiple"
              placeholder="选择 Skill"
              options={mockSkills.filter((s) => s.status === 'ENABLED').map((s) => ({
                value: s.name,
                label: s.name,
              }))}
            />
          </Form.Item>
          <Form.Item name="tools" label="绑定工具">
            <Select
              mode="multiple"
              placeholder="选择工具"
              options={mockTools.filter((t) => t.status === 'ENABLED').map((t) => ({
                value: `${t.name}(${t.type === 'MCP' ? 'MCP' : 'Function'})`,
                label: `${t.name} · ${t.type}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="sandboxName" label="代码沙箱（可选）">
            <Select
              allowClear
              placeholder="选择在线沙箱"
              options={mockSandboxes.filter((s) => s.status === 'ONLINE').map((s) => ({
                value: s.name,
                label: `${s.name} · ${s.cpu}核 / ${s.memoryMb}MB`,
              }))}
            />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  );
}
