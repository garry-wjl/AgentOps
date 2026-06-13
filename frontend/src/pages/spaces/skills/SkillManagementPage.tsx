import { PlusOutlined } from '@ant-design/icons';
import { Button, Drawer, Form, Input, Space, Table, Tag, Typography, message } from 'antd';
import { useState } from 'react';
import { mockSkills, type SkillItem, type SkillStatus } from '@/mock/skills';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<SkillStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

export default function SkillManagementPage() {
  const [list, setList] = useState<SkillItem[]>(mockSkills);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<SkillItem | null>(null);
  const [form] = Form.useForm();

  function openEdit(s: SkillItem) {
    setEditing(s);
    form.setFieldsValue(s);
    setDrawerOpen(true);
  }

  function openCreate() {
    setEditing(null);
    form.resetFields();
    setDrawerOpen(true);
  }

  function handleSubmit() {
    form.validateFields().then((v) => {
      if (editing) {
        setList((prev) => prev.map((it) => (it.id === editing.id ? { ...it, ...v } : it)));
      } else {
        setList((prev) => [
          {
            id: `sk-${Date.now()}`,
            num: `SK${Date.now()}001`,
            status: 'DRAFT',
            boundAgents: [],
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
            Skill 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            Skill 定义、注册中心与 Agent 绑定关系
          </Paragraph>
        </div>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建 Skill
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
          { title: 'Key', dataIndex: 'skillKey', render: (v) => <Text code>{v}</Text> },
          { title: '描述', dataIndex: 'description', ellipsis: true },
          {
            title: '已绑定 Agent',
            dataIndex: 'boundAgents',
            width: 180,
            render: (agents: string[]) =>
              agents.length === 0 ? (
                <Text type="secondary">未绑定</Text>
              ) : (
                <Space wrap size={4}>
                  {agents.map((a) => (
                    <Tag key={a} color="blue">
                      {a}
                    </Tag>
                  ))}
                </Space>
              ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: SkillStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          {
            title: '操作',
            width: 160,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>查看</a>
                <a onClick={() => openEdit(r)}>编辑</a>
              </Space>
            ),
          },
        ]}
      />

      <Drawer
        title={editing ? `编辑 Skill - ${editing.name}` : '新建 Skill'}
        width={560}
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
          <Form.Item label="业务编码">
            <Input value={editing?.num || '系统提交后生成'} disabled />
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true }]}>
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item name="skillKey" label="Skill Key" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="inputSchema" label="输入 Schema (JSON)">
            <Input.TextArea rows={3} placeholder='{ "field": "string" }' />
          </Form.Item>
          <Form.Item name="outputSchema" label="输出 Schema (JSON)">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  );
}
