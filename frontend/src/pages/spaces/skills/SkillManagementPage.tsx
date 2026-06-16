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
  deleteSkill,
  enableSkill,
  pageSkills,
  withdrawSkill,
  type SkillStatus,
  type SkillVO,
} from '@/api/skill';
import { ellipsisCell } from '@/utils/listCell';
import { notifyError } from '@/utils/request';

const { Title, Paragraph, Text } = Typography;

/**
 * Skill 状态展示。
 */
const STATUS: Record<SkillStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  EFFECTIVE: { color: 'green', label: '启用' },
  WITHDRAWN: { color: 'red', label: '停用' },
};

const PAGE_SIZE = 10;

/**
 * Skill 管理列表页（空间内）。
 *
 * 列表/启停/删除在本页完成；新建/编辑跳转到独立全页编辑：
 *   /spaces/:spaceId/skills/new
 *   /spaces/:spaceId/skills/:skillNum/edit
 */
export default function SkillManagementPage() {
  const navigate = useNavigate();
  const { spaceId = '' } = useParams();
  const spaceCode = spaceId;

  const [list, setList] = useState<SkillVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<SkillStatus | ''>('');
  const [loading, setLoading] = useState(false);

  const load = useCallback(
    async (nextPage = pageNo, k = keyword, s: SkillStatus | '' = statusFilter) => {
      if (!spaceCode) return;
      setLoading(true);
      try {
        const result = await pageSkills(spaceCode, {
          keyword: k || undefined,
          status: s || undefined,
          pageNo: nextPage,
          pageSize: PAGE_SIZE,
        });
        setList(result.records ?? []);
        setTotal(result.total ?? 0);
        setPageNo(result.pageNo ?? nextPage);
      } catch (err) {
        notifyError(err, '加载 Skill 列表失败');
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

  async function handleStatusAction(row: SkillVO, action: 'enable' | 'withdraw') {
    try {
      if (action === 'enable') await enableSkill(row.num);
      else await withdrawSkill(row.num);
      message.success('操作成功');
      await load(pageNo, keyword, statusFilter);
    } catch (err) {
      notifyError(err, '操作失败');
    }
  }

  async function handleDelete(row: SkillVO) {
    try {
      await deleteSkill(row.num);
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
            Skill 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 Skill 定义、注册中心与版本管理 · 仅启用态可被 Agent 引用
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索名称或标签"
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
              const s = v === 'ALL' ? '' : (v as SkillStatus);
              setStatusFilter(s);
              load(1, keyword, s);
            }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'EFFECTIVE', label: '启用' },
              { value: 'WITHDRAWN', label: '停用' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            新建 Skill
          </Button>
        </Space>
      </div>

      <Table<SkillVO>
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
            title: '描述',
            dataIndex: 'description',
            width: 280,
            render: (v: string) => ellipsisCell(v),
          },
          {
            title: '当前版本',
            dataIndex: 'currentVersionNo',
            width: 110,
            render: (v?: string) => (v ? <Text code>{v}</Text> : <Text type="secondary">—</Text>),
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
            render: (s: SkillStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          { title: '更新时间', dataIndex: 'updateTime', width: 160 },
          {
            title: '操作',
            width: 240,
            fixed: 'right',
            render: (_: unknown, r: SkillVO) => (
              <Space>
                <a onClick={() => navigate(`${r.num}/edit`)}>编辑</a>
                {r.status === 'DRAFT' && <a onClick={() => handleStatusAction(r, 'enable')}>启用</a>}
                {r.status === 'EFFECTIVE' && (
                  <a onClick={() => handleStatusAction(r, 'withdraw')}>停用</a>
                )}
                {r.status === 'DRAFT' && (
                  <Popconfirm
                    title={`确认删除 Skill「${r.name}」？`}
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
