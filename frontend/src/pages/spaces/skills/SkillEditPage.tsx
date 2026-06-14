import { ArrowLeftOutlined } from '@ant-design/icons';
import { Button, Form, Input, Space, Typography, message } from 'antd';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useSpaceResourceStore } from '@/stores/spaceResourceStore';
import type { SkillItem } from '@/mock/skills';

const { Title, Paragraph, Text } = Typography;

export default function SkillEditPage() {
  const navigate = useNavigate();
  const { skillId } = useParams();
  const isEdit = !!skillId;
  const skills = useSpaceResourceStore((s) => s.skills);
  const upsertSkill = useSpaceResourceStore((s) => s.upsertSkill);
  const editing = isEdit ? skills.find((s) => s.id === skillId) : undefined;
  const [form] = Form.useForm();

  useEffect(() => {
    if (isEdit && editing) {
      form.setFieldsValue(editing);
    }
  }, [isEdit, editing, form]);

  function handleSave() {
    form.validateFields().then((values) => {
      const now = new Date().toISOString().slice(0, 16).replace('T', ' ');
      if (isEdit && editing) {
        upsertSkill({ ...editing, ...values, updatedBy: '当前用户', updatedAt: now });
      } else {
        upsertSkill({
          id: `sk-${Date.now()}`,
          num: `SK${Date.now()}001`,
          status: 'DRAFT',
          boundAgents: [],
          updatedBy: '当前用户',
          updatedAt: now,
          ...values,
        } as SkillItem);
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
              返回 Skill 列表
            </Button>
          </Space>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑 Skill · ${editing?.name ?? ''}` : '新建 Skill'}
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
          <Input.TextArea rows={5} placeholder='{ "field": "string" }' />
        </Form.Item>
        <Form.Item name="outputSchema" label="输出 Schema (JSON)">
          <Input.TextArea rows={5} />
        </Form.Item>
      </Form>
    </div>
  );
}
