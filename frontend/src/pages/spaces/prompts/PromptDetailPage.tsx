import { EditOutlined } from '@ant-design/icons';
import { Button, Descriptions, Skeleton, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { disablePrompt, enablePrompt, getPrompt, submitPrompt, type PromptDTO, type PromptStatus } from '@/api/prompt';
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

/**
 * Prompt 详情页（只读）。
 *
 * 路由：/spaces/:spaceId/prompts/:promptNum
 */
export default function PromptDetailPage() {
  const navigate = useNavigate();
  const { spaceId = '', promptNum = '' } = useParams();
  const listPath = `/spaces/${spaceId}/prompts`;

  const [detail, setDetail] = useState<PromptDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  function loadDetail() {
    if (!promptNum) return;
    setLoading(true);
    getPrompt(promptNum)
      .then(setDetail)
      .catch((err) => {
        notifyError(err, '加载 Prompt 详情失败');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [promptNum]);

  /** 状态切换 / 提交。 */
  async function handleAction(action: 'submit' | 'enable' | 'disable') {
    if (!detail) return;
    setActionLoading(true);
    try {
      if (action === 'submit') await submitPrompt(detail.num);
      else if (action === 'enable') await enablePrompt(detail.num);
      else await disablePrompt(detail.num);
      message.success('操作成功');
      loadDetail();
    } catch (err) {
      notifyError(err, '操作失败');
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: 'Prompt 管理', to: listPath },
          { title: `详情 · ${detail?.name ?? ''}` },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Prompt 详情 · {detail?.name ?? ''}
          </Title>
          {detail && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{detail.num}</Text>
            </Paragraph>
          )}
        </div>
        <Space>
          {detail?.status === 'DRAFT' && (
            <Button onClick={() => handleAction('submit')} loading={actionLoading}>
              提交（启用）
            </Button>
          )}
          {detail?.status === 'ENABLED' && (
            <Button onClick={() => handleAction('disable')} loading={actionLoading}>
              禁用
            </Button>
          )}
          {detail?.status === 'DISABLED' && (
            <Button onClick={() => handleAction('enable')} loading={actionLoading}>
              启用
            </Button>
          )}
          <Button
            type="primary"
            icon={<EditOutlined />}
            onClick={() => navigate(`${listPath}/${promptNum}/edit`)}
          >
            编辑
          </Button>
        </Space>
      </div>

      {loading || !detail ? (
        <Skeleton active paragraph={{ rows: 10 }} />
      ) : (
        <div style={{ maxWidth: 960 }}>
          <Descriptions column={2} bordered size="middle">
            <Descriptions.Item label="名称" span={2}>
              <Text strong>{detail.name}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="Key">
              <Text code>{detail.key}</Text>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={STATUS[detail.status].color}>{STATUS[detail.status].label}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="备注" span={2}>
              {detail.remark ? <Text>{detail.remark}</Text> : <Text type="secondary">—</Text>}
            </Descriptions.Item>
            <Descriptions.Item label="变量" span={2}>
              <Space wrap size={4}>
                {(!detail.variables || detail.variables.length === 0) && (
                  <Text type="secondary">—</Text>
                )}
                {detail.variables?.map((v) => (
                  <Tag key={v} color="cyan">{`{{${v}}}`}</Tag>
                ))}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">{detail.createTime || '—'}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{detail.updateTime || '—'}</Descriptions.Item>
          </Descriptions>

          <div style={{ marginTop: 24 }}>
            <Title level={5}>内容（Markdown）</Title>
            <pre className="markdown-preview" style={{ whiteSpace: 'pre-wrap', background: '#fafafa', padding: 16, borderRadius: 4, border: '1px solid #f0f0f0' }}>
              {highlight(detail.content || '')}
            </pre>
          </div>
        </div>
      )}
    </div>
  );
}

/** 简单语法高亮：突出 {{变量}}。 */
function highlight(text: string): React.ReactNode {
  if (!text) return <Text type="secondary">（空）</Text>;
  const parts = text.split(/(\{\{[A-Za-z_][A-Za-z0-9_]{0,31}\}\})/g);
  return parts.map((p, i) =>
    /^\{\{[A-Za-z_][A-Za-z0-9_]{0,31}\}\}$/.test(p) ? (
      <span key={i} className="var-token">
        {p}
      </span>
    ) : (
      <span key={i}>{p}</span>
    ),
  );
}
