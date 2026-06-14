import { ArrowLeftOutlined } from '@ant-design/icons';
import { Button, Form, Input, Select, Space, Typography, message } from 'antd';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useSpaceResourceStore } from '@/stores/spaceResourceStore';
import type { AgentItem } from '@/mock/agents';
import { mockModels } from '@/mock/models';
import { mockPrompts } from '@/mock/prompts';
import { mockSkills } from '@/mock/skills';
import { mockTools } from '@/mock/tools';
import { mockSandboxes } from '@/mock/sandboxes';

const { Title, Paragraph, Text } = Typography;

/**
 * Agent 新建/编辑 —— 整页表单（不再使用抽屉）。
 * 路由：
 *   /spaces/:spaceId/agents/new
 *   /spaces/:spaceId/agents/:agentId/edit
 */
export default function AgentEditPage() {
  const navigate = useNavigate();
  const { agentId } = useParams();
  const isEdit = !!agentId;
  const agents = useSpaceResourceStore((s) => s.agents);
  const upsertAgent = useSpaceResourceStore((s) => s.upsertAgent);
  const editing = isEdit ? agents.find((a) => a.id === agentId) : undefined;
  const [form] = Form.useForm();

  useEffect(() => {
    if (isEdit && editing) {
      form.setFieldsValue({
        name: editing.name,
        description: editing.description,
        modelName: editing.modelName,
        promptKey: editing.promptKey,
        skills: editing.skills,
        tools: editing.tools,
        sandboxName: editing.sandboxName,
      });
    }
  }, [isEdit, editing, form]);

  function handleSave(submit: boolean) {
    form.validateFields().then((values) => {
      const now = new Date().toISOString().slice(0, 16).replace('T', ' ');
      if (isEdit && editing) {
        upsertAgent({
          ...editing,
          ...values,
          status: submit ? 'ONLINE' : editing.status,
          updatedBy: '当前用户',
          updatedAt: now,
        });
      } else {
        upsertAgent({
          id: `ag-${Date.now()}`,
          num: `AG${Date.now()}001`,
          status: submit ? 'ONLINE' : 'DRAFT',
          updatedBy: '当前用户',
          updatedAt: now,
          ...values,
        } as AgentItem);
      }
      message.success(submit ? '已部署上线' : '草稿已保存');
      navigate('..', { relative: 'path' });
    });
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Space size={4} style={{ marginBottom: 6 }}>
            <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate('..', { relative: 'path' })}>
              返回 Agent 列表
            </Button>
          </Space>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑 Agent · ${editing?.name ?? ''}` : '新建 Agent'}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text> · 状态：{editing.status}
            </Paragraph>
          )}
        </div>
        <Space>
          <Button onClick={() => navigate('..', { relative: 'path' })}>取消</Button>
          <Button onClick={() => handleSave(false)}>保存为草稿</Button>
          <Button type="primary" onClick={() => handleSave(true)}>
            {isEdit ? '保存并部署' : '保存并提交'}
          </Button>
        </Space>
      </div>

      <Form form={form} layout="vertical" style={{ maxWidth: 900 }}>
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
            options={mockModels
              .filter((m) => m.status === 'ENABLED')
              .map((m) => ({ value: m.name, label: `${m.name} · ${m.modelId}` }))}
          />
        </Form.Item>
        <Form.Item
          name="promptKey"
          label="System Prompt（按 Key 引用）"
          rules={[{ required: true }]}
        >
          <Select
            showSearch
            placeholder="选择启用态 Prompt"
            options={mockPrompts
              .filter((p) => p.status === 'ENABLED')
              .map((p) => ({ value: p.promptKey, label: `${p.promptKey} · ${p.name}` }))}
          />
        </Form.Item>
        <Form.Item name="skills" label="绑定 Skill">
          <Select
            mode="multiple"
            placeholder="选择 Skill"
            options={mockSkills
              .filter((s) => s.status === 'ENABLED')
              .map((s) => ({ value: s.name, label: s.name }))}
          />
        </Form.Item>
        <Form.Item name="tools" label="绑定工具">
          <Select
            mode="multiple"
            placeholder="选择工具"
            options={mockTools
              .filter((t) => t.status === 'ENABLED')
              .map((t) => ({
                value: `${t.name}(${t.type === 'MCP' ? 'MCP' : 'Function'})`,
                label: `${t.name} · ${t.type}`,
              }))}
          />
        </Form.Item>
        <Form.Item name="sandboxName" label="代码沙箱（可选）">
          <Select
            allowClear
            placeholder="选择在线沙箱"
            options={mockSandboxes
              .filter((s) => s.status === 'ONLINE')
              .map((s) => ({ value: s.name, label: `${s.name} · ${s.cpu}核 / ${s.memoryMb}MB` }))}
          />
        </Form.Item>
      </Form>
    </div>
  );
}
