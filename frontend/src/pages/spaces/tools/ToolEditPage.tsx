import { ArrowLeftOutlined } from '@ant-design/icons';
import { Button, Form, Input, Select, Space, Typography, message } from 'antd';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useSpaceResourceStore } from '@/stores/spaceResourceStore';
import type { ToolItem } from '@/mock/tools';

const { Title, Paragraph, Text } = Typography;

export default function ToolEditPage() {
  const navigate = useNavigate();
  const { toolId } = useParams();
  const isEdit = !!toolId;
  const tools = useSpaceResourceStore((s) => s.tools);
  const upsertTool = useSpaceResourceStore((s) => s.upsertTool);
  const editing = isEdit ? tools.find((t) => t.id === toolId) : undefined;
  const [form] = Form.useForm();

  useEffect(() => {
    if (isEdit && editing) {
      form.setFieldsValue(editing);
    } else {
      form.setFieldsValue({ type: 'MCP', authType: 'API_KEY' });
    }
  }, [isEdit, editing, form]);

  function handleSave() {
    form.validateFields().then((values) => {
      const now = new Date().toISOString().slice(0, 16).replace('T', ' ');
      if (isEdit && editing) {
        upsertTool({ ...editing, ...values, updatedBy: '当前用户', updatedAt: now });
      } else {
        upsertTool({
          id: `tl-${Date.now()}`,
          num: `TL${Date.now()}001`,
          status: 'ENABLED',
          health: 'UNKNOWN',
          updatedBy: '当前用户',
          updatedAt: now,
          ...values,
        } as ToolItem);
      }
      message.success('已保存');
      navigate('..', { relative: 'path' });
    });
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Space size={4} style={{ marginBottom: 6 }}>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('..', { relative: 'path' })}
            >
              返回工具列表
            </Button>
          </Space>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑工具 · ${editing?.name ?? ''}` : '注册工具'}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text>
            </Paragraph>
          )}
        </div>
        <Space>
          <Button onClick={() => navigate('..', { relative: 'path' })}>取消</Button>
          <Button type="primary" onClick={handleSave}>
            保存
          </Button>
        </Space>
      </div>

      <Form form={form} layout="vertical" style={{ maxWidth: 900 }}>
        <Form.Item label="业务编码">
          <Input value={editing?.num || '系统提交后生成'} disabled />
        </Form.Item>
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
          <Input.TextArea rows={3} />
        </Form.Item>
      </Form>
    </div>
  );
}
