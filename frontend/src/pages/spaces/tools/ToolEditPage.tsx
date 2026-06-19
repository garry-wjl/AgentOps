import { Button, Form, Input, Select, Skeleton, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createTool,
  getTool,
  updateTool,
  type ToolDTO,
  type ToolSubType,
  type ToolType,
} from '@/api/tool';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

interface ToolFormValues {
  name: string;
  type: ToolType;
  subType: ToolSubType;
  configJson: string;
  description?: string;
  tags?: string[];
  remark?: string;
}

const TYPE_OPTIONS: { value: ToolType; label: string }[] = [
  { value: 'FUNCTION_CALL', label: 'Function Call' },
  { value: 'MCP', label: 'MCP 协议' },
];

const SUBTYPE_OPTIONS: { value: ToolSubType; label: string }[] = [
  { value: 'OPENAPI', label: 'OpenAPI 规范' },
  { value: 'ENDPOINT', label: 'HTTP Endpoint' },
  { value: 'REMOTE', label: '远程服务' },
  { value: 'LOCAL', label: '本地工具' },
];

/**
 * 工具注册/编辑 —— 整页表单。
 *
 * 路由：
 *   /spaces/:spaceId/tools/new
 *   /spaces/:spaceId/tools/:toolNum/edit
 */
export default function ToolEditPage() {
  const navigate = useNavigate();
  const { spaceId = '', toolNum } = useParams();
  const spaceCode = spaceId;
  const isEdit = !!toolNum;
  const listPath = `/spaces/${spaceId}/tools`;

  const [editing, setEditing] = useState<ToolDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<ToolFormValues>();

  useEffect(() => {
    if (!isEdit || !toolNum) {
      form.setFieldsValue({ type: 'MCP', subType: 'REMOTE' });
      return;
    }
    setLoading(true);
    getTool(toolNum)
      .then((detail) => {
        setEditing(detail);
        form.setFieldsValue({
          name: detail.name,
          type: detail.type,
          subType: detail.subType,
          configJson: detail.configJson ?? '',
          description: detail.description ?? '',
          tags: detail.tags ?? [],
          remark: detail.remark ?? '',
        });
      })
      .catch((err) => {
        notifyError(err, '加载工具详情失败');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }, [isEdit, toolNum, form, navigate, listPath]);

  async function handleSave() {
    let values: ToolFormValues;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      if (isEdit && editing) {
        await updateTool(editing.num, {
          name: values.name,
          configJson: values.configJson,
          description: values.description,
          tags: values.tags,
          remark: values.remark,
        });
        message.success('已保存');
      } else {
        await createTool(spaceCode, {
          name: values.name,
          type: values.type,
          subType: values.subType,
          configJson: values.configJson,
          description: values.description,
          tags: values.tags,
          remark: values.remark,
        });
        message.success('工具已注册（草稿）');
      }
      navigate(listPath);
    } catch (err) {
      notifyError(err, isEdit ? '保存失败' : '注册失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: '工具管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '注册工具' },
        ]}
      />
      <div className="page-header">
        <div>
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
          <Button onClick={() => navigate(listPath)} disabled={submitting}>
            取消
          </Button>
          <Button type="primary" onClick={handleSave} loading={submitting}>
            保存
          </Button>
        </Space>
      </div>

      {loading ? (
        <Skeleton active paragraph={{ rows: 6 }} />
      ) : (
        <Form form={form} layout="vertical" style={{ maxWidth: 900 }}>
          <Form.Item
            name="name"
            label="工具名称"
            rules={[{ required: true, message: '请输入名称' }, { max: 50 }]}
          >
            <Input maxLength={50} placeholder="例如：网络搜索" />
          </Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true }]}>
            <Select options={TYPE_OPTIONS} disabled={isEdit} />
          </Form.Item>
          <Form.Item name="subType" label="子类型" rules={[{ required: true }]}>
            <Select options={SUBTYPE_OPTIONS} disabled={isEdit} />
          </Form.Item>
          <Form.Item
            name="configJson"
            label="配置（JSON）"
            rules={[{ required: true, message: '请填写配置' }]}
            extra="敏感字段（如 API Key）保存时会被加密；编辑时如未改动则保留原值"
          >
            <Input.TextArea
              rows={10}
              placeholder={'{\n  "endpoint": "https://...",\n  "apiKey": "****",\n  "method": "POST"\n}'}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} maxLength={200} showCount />
          </Form.Item>
          <Form.Item name="tags" label="标签">
            <Select
              mode="tags"
              placeholder="按回车键确认标签"
              maxCount={10}
              tokenSeparators={[',', '，']}
            />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} showCount />
          </Form.Item>
        </Form>
      )}
    </div>
  );
}
