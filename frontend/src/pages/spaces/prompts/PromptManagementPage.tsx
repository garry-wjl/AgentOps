import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Input, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  deletePrompt,
  disablePrompt,
  enablePrompt,
  pagePrompts,
  submitPrompt,
  type PromptStatus,
  type PromptVO,
} from '@/api/prompt';
import { notifyError } from '@/utils/request';

const { Title, Paragraph, Text } = Typography;

/**
 * Prompt 状态展示。
 */
const STATUS: Record<PromptStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

const PAGE_SIZE = 10;

/**
 * Prompt 管理列表页（空间内）。
 *
 * 列表/启停/删除在本页完成；新建/编辑跳转到独立全页编辑：
 *   /spaces/:spaceId/prompts/new
 *   /spaces/:spaceId/prompts/:promptNum/edit
 */
export default function PromptManagementPage() {
  const navigate = useNavigate();
  const { spaceId = '' } = useParams();
  const spaceCode = spaceId;

  const [list, setList] = useState<PromptVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<PromptStatus | ''>('');
  const [loading, setLoading] = useState(false);

  /** 加载分页列表。 */
  const load = useCallback(
    async (nextPage = pageNo, k = keyword, s: PromptStatus | '' = statusFilter) => {
      if (!spaceCode) return;
      setLoading(true);
      try {
        const result = await pagePrompts(spaceCode, {
          keyword: k || undefined,
          status: s || undefined,
          pageNo: nextPage,
          pageSize: PAGE_SIZE,
        });
        setList(result.records ?? []);
        setTotal(result.total ?? 0);
        setPageNo(result.pageNo ?? nextPage);
      } catch (err) {
        notifyError(err, '加载 Prompt 列表失败');
      } finally {
        setLoading(false);
      }
    },
    [spaceCode, pageNo, keyword, statusFilter],
  );

  useEffect(() => {
    load(1, '', '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [spaceCode]);

  /** 列表行内"提交/启用/禁用"。 */
  async function handleStatusAction(row: PromptVO, action: 'submit' | 'enable' | 'disable') {
    try {
      if (action === 'submit') await submitPrompt(row.num);
      else if (action === 'enable') await enablePrompt(row.num);
      else await disablePrompt(row.num);
      message.success('操作成功');
      await load(pageNo, keyword, statusFilter);
    } catch (err) {
      notifyError(err, '操作失败');
    }
  }

  async function handleDelete(row: PromptVO) {
    try {
      await deletePrompt(row.num);
      message.success('已删除');
      const newTotal = total - 1;
      const lastPage = Math.max(1, Math.ceil(newTotal / PAGE_SIZE));
      await load(Math.min(pageNo, lastPage), keyword, statusFilter);
    } catch (err) {
      notifyError(err, '删除失败');
    }
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Prompt 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 Prompt 资产 · Markdown 内容支持 <Text code>{'{{变量}}'}</Text> 占位 · 仅启用态可被 Agent 引用
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索名称/Key/备注"
            allowClear
            onSearch={(v) => {
              setKeyword(v);
              load(1, v, statusFilter);
            }}
            style={{ width: 260 }}
          />
          <Select
            value={statusFilter || 'ALL'}
            style={{ width: 120 }}
            onChange={(v) => {
              const s = v === 'ALL' ? '' : (v as PromptStatus);
              setStatusFilter(s);
              load(1, keyword, s);
            }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'ENABLED', label: '启用' },
              { value: 'DISABLED', label: '禁用' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            新建 Prompt
          </Button>
        </Space>
      </div>

      <Table<PromptVO>
        rowKey="num"
        loading={loading}
        dataSource={list}
        pagination={{
          current: pageNo,
          total,
          pageSize: PAGE_SIZE,
          showSizeChanger: false,
          onChange: (p) => load(p, keyword, statusFilter),
        }}
        scroll={{ x: 1280 }}
        tableLayout="fixed"
        columns={[
          {
            title: '编码',
            dataIndex: 'num',
            width: 220,
            fixed: 'left',
            render: (v: string) => <Text code>{v}</Text>,
          },
          {
            title: '名称',
            dataIndex: 'name',
            width: 200,
            render: (v: string) => <Text strong>{v}</Text>,
          },
          { title: 'Key', dataIndex: 'key', width: 220, render: (v: string) => <Text code>{v}</Text> },
          {
            title: '变量',
            dataIndex: 'variables',
            width: 220,
            render: (vars?: string[]) => (
              <Space wrap size={4}>
                {(!vars || vars.length === 0) && <Text type="secondary">—</Text>}
                {vars?.map((v) => (
                  <Tag key={v} color="cyan">{`{{${v}}}`}</Tag>
                ))}
              </Space>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: PromptStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          { title: '更新时间', dataIndex: 'updateTime', width: 160 },
          {
            title: '操作',
            width: 280,
            fixed: 'right',
            render: (_: unknown, r: PromptVO) => (
              <Space>
                <a onClick={() => navigate(`${r.num}`)}>详情</a>
                <a onClick={() => navigate(`${r.num}/edit`)}>编辑</a>
                {r.status === 'DRAFT' && <a onClick={() => handleStatusAction(r, 'submit')}>提交</a>}
                {r.status === 'ENABLED' && <a onClick={() => handleStatusAction(r, 'disable')}>禁用</a>}
                {r.status === 'DISABLED' && <a onClick={() => handleStatusAction(r, 'enable')}>启用</a>}
                {r.status === 'DRAFT' && (
                  <Popconfirm
                    title="确认删除该 Prompt？"
                    okText="删除"
                    okButtonProps={{ danger: true }}
                    cancelText="取消"
                    onConfirm={() => handleDelete(r)}
                  >
                    <a style={{ color: '#ff4d4f' }}>
                      <DeleteOutlined /> 删除
                    </a>
                  </Popconfirm>
                )}
              </Space>
            ),
          },
        ]}
      />
    </div>
  );
}
