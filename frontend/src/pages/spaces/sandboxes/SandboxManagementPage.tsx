import { DeleteOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import {
  Badge,
  Button,
  Input,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  deleteSandbox,
  disableSandbox,
  pageSandboxes,
  reEnableSandbox,
  submitSandbox,
  type SandboxStatus,
  type SandboxVO,
} from '@/api/sandbox';
import { notifyError } from '@/utils/request';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<SandboxStatus, { color: string; label: string; icon?: React.ReactNode }> = {
  DRAFT: { color: 'default', label: '草稿' },
  INITIALIZING: { color: 'processing', label: '初始化中', icon: <LoadingOutlined /> },
  ONLINE: { color: 'success', label: '在线' },
  OFFLINE: { color: 'warning', label: '离线' },
  DISABLED: { color: 'error', label: '禁用' },
};

const PAGE_SIZE = 10;

export default function SandboxManagementPage() {
  const navigate = useNavigate();
  const { spaceId = '' } = useParams();
  const spaceCode = spaceId;

  const [list, setList] = useState<SandboxVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<SandboxStatus | ''>('');
  const [loading, setLoading] = useState(false);

  const load = useCallback(
    async (nextPage = pageNo, k = keyword, s: SandboxStatus | '' = statusFilter) => {
      if (!spaceCode) return;
      setLoading(true);
      try {
        const result = await pageSandboxes(spaceCode, {
          keyword: k || undefined,
          status: s || undefined,
          pageNo: nextPage,
          pageSize: PAGE_SIZE,
        });
        setList(result.records ?? []);
        setTotal(result.total ?? 0);
        setPageNo(result.pageNo ?? nextPage);
      } catch (err) {
        notifyError(err, '加载沙箱列表失败');
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

  async function handleAction(row: SandboxVO, action: 'submit' | 'disable' | 'reEnable') {
    try {
      if (action === 'submit') await submitSandbox(row.num);
      else if (action === 'disable') await disableSandbox(row.num);
      else await reEnableSandbox(row.num);
      message.success('操作成功');
      await load(pageNo, keyword, statusFilter);
    } catch (err) {
      notifyError(err, '操作失败');
    }
  }

  async function handleDelete(row: SandboxVO) {
    try {
      await deleteSandbox(row.num);
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
            沙箱管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            代码沙箱的注册、提交、探活与启停
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
            style={{ width: 140 }}
            onChange={(v) => {
              const s = v === 'ALL' ? '' : (v as SandboxStatus);
              setStatusFilter(s);
              load(1, keyword, s);
            }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'INITIALIZING', label: '初始化中' },
              { value: 'ONLINE', label: '在线' },
              { value: 'OFFLINE', label: '离线' },
              { value: 'DISABLED', label: '禁用' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            注册沙箱
          </Button>
        </Space>
      </div>

      <Table<SandboxVO>
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
            title: '镜像',
            dataIndex: 'image',
            width: 280,
            render: (v: string) => <Text code style={{ wordBreak: 'break-all' }}>{v}</Text>,
          },
          {
            title: '接入地址覆盖',
            dataIndex: 'baseUrlOverride',
            width: 220,
            render: (v?: string) =>
              v ? <Text code style={{ wordBreak: 'break-all' }}>{v}</Text> : <Text type="secondary">—</Text>,
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 130,
            render: (s: SandboxStatus) => {
              const cfg = STATUS[s];
              return (
                <Tooltip title={cfg.label}>
                  <Badge
                    status={cfg.color as any}
                    text={
                      <Space size={4}>
                        {cfg.icon}
                        {cfg.label}
                      </Space>
                    }
                  />
                </Tooltip>
              );
            },
          },
          {
            title: '最近心跳',
            dataIndex: 'lastHeartbeatTime',
            width: 170,
            render: (v?: string) =>
              v ? <Text>{v}</Text> : <Text type="secondary">—</Text>,
          },
          { title: '更新时间', dataIndex: 'updateTime', width: 170 },
          {
            title: '操作',
            width: 280,
            fixed: 'right',
            render: (_: unknown, r: SandboxVO) => (
              <Space>
                <a onClick={() => navigate(`${r.num}/edit`)}>编辑</a>
                {r.status === 'DRAFT' && <a onClick={() => handleAction(r, 'submit')}>提交</a>}
                {r.status !== 'DRAFT' && r.status !== 'DISABLED' && (
                  <a onClick={() => handleAction(r, 'disable')}>禁用</a>
                )}
                {r.status === 'DISABLED' && (
                  <a onClick={() => handleAction(r, 'reEnable')}>重新启用</a>
                )}
                {r.status === 'DRAFT' && (
                  <Popconfirm
                    title={`确认删除沙箱「${r.name}」？`}
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
