import { Button, Form, Input, Skeleton, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createSandbox,
  getSandbox,
  updateSandbox,
  type SandboxDTO,
} from '@/api/sandbox';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

interface SandboxFormValues {
  name: string;
  image: string;
  baseUrlOverride?: string;
  remark?: string;
}

/**
 * 沙箱注册/编辑 —— 整页表单。
 *
 * 路由：
 *   /spaces/:spaceId/sandboxes/new
 *   /spaces/:spaceId/sandboxes/:sandboxNum/edit
 */
export default function SandboxEditPage() {
  const navigate = useNavigate();
  const { spaceId = '', sandboxNum } = useParams();
  const spaceCode = spaceId;
  const isEdit = !!sandboxNum;
  const listPath = `/spaces/${spaceId}/sandboxes`;

  const [editing, setEditing] = useState<SandboxDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<SandboxFormValues>();

  useEffect(() => {
    if (!isEdit || !sandboxNum) return;
    setLoading(true);
    getSandbox(sandboxNum)
      .then((detail) => {
        setEditing(detail);
        form.setFieldsValue({
          name: detail.name,
          image: detail.image,
          baseUrlOverride: detail.baseUrlOverride ?? '',
          remark: detail.remark ?? '',
        });
      })
      .catch((err) => {
        notifyError(err, '加载沙箱详情失败');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }, [isEdit, sandboxNum, form, navigate, listPath]);

  async function handleSave() {
    let values: SandboxFormValues;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      if (isEdit && editing) {
        await updateSandbox(editing.num, {
          name: values.name,
          image: values.image,
          baseUrlOverride: values.baseUrlOverride,
          remark: values.remark,
        });
        message.success('已保存');
      } else {
        await createSandbox(spaceCode, {
          name: values.name,
          image: values.image,
          baseUrlOverride: values.baseUrlOverride,
          remark: values.remark,
        });
        message.success('沙箱已注册（草稿）');
      }
      navigate(listPath);
    } catch (err) {
      notifyError(err, isEdit ? '保存失败' : '注册失败');
    } finally {
      setSubmitting(false);
    }
  }

  const statusTag = editing
    ? (
      <Tag
        color={
          editing.status === 'ONLINE'
            ? 'green'
            : editing.status === 'OFFLINE'
            ? 'orange'
            : editing.status === 'DISABLED'
            ? 'red'
            : editing.status === 'INITIALIZING'
            ? 'blue'
            : 'default'
        }
        style={{ marginLeft: 8 }}
      >
        {editing.status}
      </Tag>
    )
    : null;

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: '沙箱管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '注册沙箱' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑沙箱 · ${editing?.name ?? ''}` : '注册沙箱'}
            {statusTag}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text>
              {editing.lastStatusReason && (
                <span> · 最近状态：<Text>{editing.lastStatusReason}</Text></span>
              )}
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
            label="沙箱名称"
            rules={[{ required: true, message: '请输入名称' }, { max: 50 }]}
          >
            <Input maxLength={50} placeholder="例如：代码执行沙箱-A" />
          </Form.Item>
          <Form.Item
            name="image"
            label="镜像"
            rules={[{ required: true, message: '请输入镜像' }]}
            extra="Docker 镜像地址，例如 sandbox-runner:latest"
          >
            <Input placeholder="sandbox-runner:latest" />
          </Form.Item>
          <Form.Item
            name="baseUrlOverride"
            label="接入地址覆盖（可选）"
            extra="留空时使用系统设置中的默认 BaseURL"
          >
            <Input placeholder="http://sandbox-runtime:8080" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} showCount />
          </Form.Item>
        </Form>
      )}
    </div>
  );
}
