import { Button, Form, Input, Skeleton, Space, Tag, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createPrompt,
  getPrompt,
  submitPrompt,
  updatePrompt,
  type PromptDTO,
  type PromptStatus,
} from '@/api/prompt';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

/**
 * Prompt 状态展示。
 */
const STATUS: Record<PromptStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

interface PromptFormValues {
  name: string;
  key: string;
  remark?: string;
}

/**
 * Prompt 新建/编辑 —— 整页表单。
 *
 * 路由：
 *   /spaces/:spaceId/prompts/new
 *   /spaces/:spaceId/prompts/:promptNum/edit
 */
export default function PromptEditPage() {
  const navigate = useNavigate();
  const { spaceId = '', promptNum } = useParams();
  const spaceCode = spaceId;
  const isEdit = !!promptNum;
  const listPath = `/spaces/${spaceId}/prompts`;

  const [editing, setEditing] = useState<PromptDTO | null>(null);
  const [content, setContent] = useState('');
  const [originalKey, setOriginalKey] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<PromptFormValues>();

  const variables = useMemo(() => parseVariables(content), [content]);

  useEffect(() => {
    if (!isEdit || !promptNum) return;
    setLoading(true);
    getPrompt(promptNum)
      .then((detail) => {
        setEditing(detail);
        setContent(detail.content ?? '');
        setOriginalKey(detail.key ?? '');
        form.setFieldsValue({
          name: detail.name,
          key: detail.key,
          remark: detail.remark ?? '',
        });
      })
      .catch((err) => {
        notifyError(err, '加载 Prompt 详情失败');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }, [isEdit, promptNum, form, navigate, listPath]);

  /**
   * 保存。
   * @param submit 是否提交（仅创建/草稿状态有意义）
   */
  async function handleSave(submit: boolean) {
    let values: PromptFormValues;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      let target: PromptDTO;
      if (isEdit && editing) {
        const updateData: { name: string; content: string; remark?: string; newKey?: string } = {
          name: values.name,
          content,
          remark: values.remark,
        };
        if (editing.status === 'DRAFT' && values.key && values.key !== originalKey) {
          updateData.newKey = values.key;
        }
        target = await updatePrompt(editing.num, updateData);
        if (submit && target.status === 'DRAFT') {
          target = await submitPrompt(target.num);
          message.success('已提交，状态：启用');
        } else {
          message.success('已保存');
        }
      } else {
        target = await createPrompt(spaceCode, {
          name: values.name,
          key: values.key,
          content,
          remark: values.remark,
        });
        if (submit) {
          target = await submitPrompt(target.num);
          message.success('已提交，状态：启用');
        } else {
          message.success('草稿已保存');
        }
      }
      navigate(listPath);
    } catch (err) {
      notifyError(err, isEdit ? '保存失败' : '创建失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: 'Prompt 管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '新建 Prompt' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑 Prompt · ${editing?.name ?? ''}` : '新建 Prompt'}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text>
            </Paragraph>
          )}
        </div>
        <Space>
          <Button
            onClick={() => navigate(listPath)}
            disabled={submitting}
          >
            取消
          </Button>
          {(!isEdit || editing?.status === 'DRAFT') && (
            <>
              <Button onClick={() => handleSave(false)} loading={submitting}>
                保存为草稿
              </Button>
              <Button type="primary" onClick={() => handleSave(true)} loading={submitting}>
                保存并提交
              </Button>
            </>
          )}
          {isEdit && editing && editing.status !== 'DRAFT' && (
            <Button type="primary" onClick={() => handleSave(false)} loading={submitting}>
              保存
            </Button>
          )}
        </Space>
      </div>

      {loading ? (
        <Skeleton active paragraph={{ rows: 10 }} />
      ) : (
        <Form form={form} layout="vertical" style={{ maxWidth: 960 }}>
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: '请输入名称' }, { max: 50 }]}
          >
            <Input maxLength={50} placeholder="例如：客服开场白" />
          </Form.Item>
          <Form.Item
            name="key"
            label="Key"
            rules={[
              { required: true, message: '请输入 Key' },
              { pattern: /^[A-Za-z0-9_-]{1,64}$/, message: '英文字母/数字/下划线/中划线，1～64 字符' },
            ]}
            extra={isEdit && editing && editing.status !== 'DRAFT' ? '仅草稿态可修改 Key' : undefined}
          >
            <Input
              placeholder="customer_service_opening"
              disabled={isEdit && editing ? editing.status !== 'DRAFT' : false}
            />
          </Form.Item>
          {isEdit && editing && (
            <Form.Item label="状态">
              <Tag color={STATUS[editing.status].color}>{STATUS[editing.status].label}</Tag>
            </Form.Item>
          )}
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} showCount />
          </Form.Item>
          <Form.Item label="内容（Markdown，支持 {{变量}} 占位）" required>
            <Input.TextArea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={18}
              placeholder="Markdown 内容，支持 {{变量}} 占位"
            />
          </Form.Item>
          <Form.Item label="变量列表（保存时由后端解析）">
            <Space wrap size={4}>
              {variables.length === 0 && <Text type="secondary">暂无</Text>}
              {variables.map((v) => (
                <Tag key={v} color="cyan">{`{{${v}}}`}</Tag>
              ))}
            </Space>
          </Form.Item>
        </Form>
      )}
    </div>
  );
}

/** 解析 Markdown 中的 {{变量}}，返回去重后的变量名列表。 */
function parseVariables(text: string): string[] {
  const set = new Set<string>();
  const re = /\{\{([A-Za-z_][A-Za-z0-9_]{0,31})\}\}/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text))) set.add(m[1]);
  return Array.from(set);
}
