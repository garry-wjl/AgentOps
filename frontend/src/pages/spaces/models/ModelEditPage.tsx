import { Button, Form, Input, Skeleton, Space, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createModel, getModel, updateModel, type ModelDTO } from '@/api/model';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

interface ModelFormValues {
  name: string;
  modelId: string;
  baseUrl: string;
  apiKey: string;
  remark?: string;
}

/**
 * 模型新建/编辑 —— 整页表单。
 *
 * 路由：
 *   /spaces/:spaceId/models/new
 *   /spaces/:spaceId/models/:modelNum/edit
 */
export default function ModelEditPage() {
  const navigate = useNavigate();
  const { spaceId = '', modelNum } = useParams();
  const spaceCode = spaceId;
  const isEdit = !!modelNum;
  const listPath = `/spaces/${spaceId}/models`;

  const [editing, setEditing] = useState<ModelDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<ModelFormValues>();

  useEffect(() => {
    if (!isEdit || !modelNum) return;
    setLoading(true);
    getModel(modelNum)
      .then((detail) => {
        setEditing(detail);
        form.setFieldsValue({
          name: detail.name,
          modelId: detail.modelId,
          baseUrl: detail.baseUrl,
          // 已配置 API Key：用脱敏值占位，提交时若未变更则后端会跳过
          apiKey: detail.apiKey ?? '',
          remark: detail.remark ?? '',
        });
      })
      .catch((err) => {
        notifyError(err, '加载模型详情失败');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }, [isEdit, modelNum, form, navigate, listPath]);

  async function handleSave() {
    let values: ModelFormValues;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      if (isEdit && editing) {
        await updateModel(editing.num, {
          name: values.name,
          modelId: values.modelId,
          baseUrl: values.baseUrl,
          apiKey: values.apiKey,
          remark: values.remark,
        });
        message.success('已更新模型');
      } else {
        await createModel(spaceCode, {
          name: values.name,
          modelId: values.modelId,
          baseUrl: values.baseUrl,
          apiKey: values.apiKey,
          remark: values.remark,
        });
        message.success('模型已创建');
      }
      navigate(listPath);
    } catch (err) {
      notifyError(err, isEdit ? '更新模型失败' : '创建模型失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: '模型管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '新建模型' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑模型 · ${editing?.name ?? ''}` : '新建模型'}
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
        <Form form={form} layout="vertical" style={{ maxWidth: 700 }}>
          <Form.Item
            name="name"
            label="模型名称"
            rules={[{ required: true, message: '请输入模型名称' }, { max: 50 }]}
          >
            <Input maxLength={50} placeholder="例如：OpenAI GPT-4o" />
          </Form.Item>
          <Form.Item
            name="modelId"
            label="Model ID"
            rules={[{ required: true, message: '请输入 Model ID' }]}
          >
            <Input placeholder="例如：gpt-4o" />
          </Form.Item>
          <Form.Item
            name="baseUrl"
            label="Base URL"
            rules={[{ required: true, message: '请输入 Base URL' }]}
          >
            <Input placeholder="https://api.openai.com/v1" />
          </Form.Item>
          <Form.Item
            name="apiKey"
            label="API Key"
            rules={isEdit ? [] : [{ required: true, message: '请输入 API Key' }]}
            extra={isEdit ? '已配置则展示为脱敏值，保持不变即可；如需更换请输入新值' : undefined}
          >
            <Input.Password
              placeholder={isEdit && editing?.apiKey ? `${editing.apiKey}（保持不变可不修改）` : '请输入'}
              autoComplete="off"
            />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} maxLength={200} showCount />
          </Form.Item>
        </Form>
      )}
    </div>
  );
}
