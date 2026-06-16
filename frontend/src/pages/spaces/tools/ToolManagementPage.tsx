import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import {
  Button,
  Input,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  deleteTool,
  pageTools,
  publishTool,
  republishTool,
  withdrawTool,
  type ToolStatus,
  type ToolVO,
} from '@/api/tool';
import { notifyError } from '@/utils/request';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<ToolStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  EFFECTIVE: { color: 'green', label: '发布' },
  WITHDRAWN: { color: 'red', label: '停用' },
};

const TYPE_LABEL: Record<string, string> = {
  FUNCTION_CALL: 'Function Call',
  MCP: 'MCP',
};

const PAGE_SIZE = 10;

export default function ToolManagementPage() {
  const navigate = useNavigate();
  const { spaceId = '' } = useParams();
  const spaceCode = spaceId;

  const [list, setList] = useState<ToolVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<ToolStatus | ''>('');
  const [loading, setLoading] = useState(false);

  const load = useCallback(
    async (nextPage = pageNo, k = keyword, s: ToolStatus | '' = statusFilter) => {
      if (!spaceCode) return;
      setLoading(true);
      try {
        const result = await pageTools(spaceCode, {
          keyword: k || undefined,
          status: s || undefined,
          pageNo: nextPage,
          pageSize: PAGE_SIZE,
        });
        setList(result.records ?? []);
        setTotal(result.total ?? 0);
        setPageNo(result.pageNo ?? nextPage);
      } catch (err) {
        notifyError(err, '加载工具列表失败');
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

  async function handleAction(row: ToolVO, action: 'publish' | 'withdraw' | 'republish') {
    try {
      if (action === 'publish') await publishTool(row.num);
      else if (action === 'withdraw') await withdrawTool(row.num);
      else await republishTool(row.num);
      message.success('操作成功');
      await load(pageNo, keyword, statusFilter);
    } catch (err) {
      notifyError(err, '操作失败');
    }
  }

  async function handleDelete(row: ToolVO) {
    try {
      await deleteTool(row.num);
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
            工具管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            MCP 协议工具与 Function Call 工具的注册、试运行、发布管理
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索名称"
            allowClear
            onSearch={(v) => {
              setKeyword(v);
              load(1, v, statusFilter);
            }}
            style={{ width: 240 }}
          />
          <Select
            value={statusFilter || 'ALL'}
            style={{ width: 120 }}
            onChange={(v) => {
              const s = v === 'ALL' ? '' : (v as ToolStatus);
              setStatusFilter(s);
              load(1, keyword, s);
            }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'EFFECTIVE', label: '发布' },
              { value: 'WITHDRAWN', label: '停用' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            注册工具
          </Button>
        </Space>
      </div>

      <Table<ToolVO>
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
        scroll={{ x: 1200 }}
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
          {
            title: '类型 / 子类型',
            width: 200,
            render: (_: unknown, r: ToolVO) => (
              <Space size={4}>
                <Tag color={r.type === 'MCP' ? 'geekblue' : 'gold'}>{TYPE_LABEL[r.type] ?? r.type}</Tag>
                <Text type="secondary">{r.subType}</Text>
              </Space>
            ),
          },
          {
            title: '描述',
            dataIndex: 'description',
            width: 280,
            render: (v?: string) => (v ? <Text>{v}</Text> : <Text type="secondary">—</Text>),
          },
          {
            title: '标签',
            dataIndex: 'tags',
            width: 200,
            render: (tags?: string[]) => (
              <Space wrap size={4}>
                {(!tags || tags.length === 0) && <Text type="secondary">—</Text>}
                {tags?.map((t) => (
                  <Tag key={t}>{t}</Tag>
                ))}
              </Space>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: ToolStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          { title: '更新时间', dataIndex: 'updateTime', width: 160 },
          {
            title: '操作',
            width: 280,
            fixed: 'right',
            render: (_: unknown, r: ToolVO) => (
              <Space>
                <a onClick={() => navigate(`${r.num}/edit`)}>编辑</a>
                <a onClick={() => navigate(`${r.num}/test`)}>试运行</a>
                {r.status === 'DRAFT' && <a onClick={() => handleAction(r, 'publish')}>发布</a>}
                {r.status === 'EFFECTIVE' && (
                  <a onClick={() => handleAction(r, 'withdraw')}>撤回</a>
                )}
                {r.status === 'WITHDRAWN' && (
                  <a onClick={() => handleAction(r, 'republish')}>重新发布</a>
                )}
                {r.status === 'DRAFT' && (
                  <Popconfirm
                    title={`确认删除工具「${r.name}」？`}
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
