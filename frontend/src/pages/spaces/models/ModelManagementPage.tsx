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
  deleteModel,
  disableModel,
  enableModel,
  pageModels,
  type ModelStatus,
  type ModelVO,
} from '@/api/model';
import { ellipsisCell } from '@/utils/listCell';
import { notifyError } from '@/utils/request';

const { Title, Paragraph, Text } = Typography;

/**
 * 状态枚举展示配置。
 */
const STATUS_OPTIONS: Record<ModelStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

const PAGE_SIZE = 10;

/**
 * 模型管理列表页（空间内）。
 *
 * 列表/启停/删除在本页完成；新建/编辑跳转到独立全页编辑：
 *   /spaces/:spaceId/models/new
 *   /spaces/:spaceId/models/:modelNum/edit
 */
export default function ModelManagementPage() {
  const navigate = useNavigate();
  const { spaceId = '' } = useParams();
  const spaceCode = spaceId;

  const [list, setList] = useState<ModelVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<ModelStatus | ''>('');
  const [loading, setLoading] = useState(false);

  const load = useCallback(
    async (nextPage = pageNo, k = keyword, s: ModelStatus | '' = statusFilter) => {
      if (!spaceCode) return;
      setLoading(true);
      try {
        const result = await pageModels(spaceCode, {
          keyword: k || undefined,
          status: s || undefined,
          pageNo: nextPage,
          pageSize: PAGE_SIZE,
        });
        setList(result.records ?? []);
        setTotal(result.total ?? 0);
        setPageNo(result.pageNo ?? nextPage);
      } catch (err) {
        notifyError(err, '加载模型列表失败');
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

  async function handleToggle(row: ModelVO) {
    try {
      if (row.status === 'ENABLED') {
        await disableModel(row.num);
        message.success('已禁用');
      } else {
        await enableModel(row.num);
        message.success('已启用');
      }
      await load(pageNo, keyword, statusFilter);
    } catch (err) {
      notifyError(err, '操作失败');
    }
  }

  async function handleDelete(row: ModelVO) {
    try {
      await deleteModel(row.num);
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
            模型管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 LLM 模型供应商配置、API Key 与启用/禁用
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索模型名称或 Model ID"
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
              const s = v === 'ALL' ? '' : (v as ModelStatus);
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
            新建模型
          </Button>
        </Space>
      </div>

      <Table<ModelVO>
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
          { title: 'Model ID', dataIndex: 'modelId', width: 180 },
          {
            title: 'Base URL',
            dataIndex: 'baseUrl',
            width: 280,
            render: (v: string) => ellipsisCell(v),
          },
          {
            title: 'API Key',
            dataIndex: 'apiKey',
            width: 140,
            render: (v: string) =>
              v ? <Tag color="blue">{v}</Tag> : <Tag>未配置</Tag>,
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: ModelStatus) => (
              <Tag color={STATUS_OPTIONS[s].color}>{STATUS_OPTIONS[s].label}</Tag>
            ),
          },
          {
            title: '更新时间',
            dataIndex: 'updateTime',
            width: 160,
          },
          {
            title: '操作',
            width: 220,
            fixed: 'right',
            render: (_: unknown, r: ModelVO) => (
              <Space>
                <a onClick={() => navigate(`${r.num}/edit`)}>编辑</a>
                <a onClick={() => handleToggle(r)}>
                  {r.status === 'ENABLED' ? '禁用' : '启用'}
                </a>
                {r.status === 'DRAFT' && (
                  <Popconfirm
                    title="确认删除该模型？"
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
