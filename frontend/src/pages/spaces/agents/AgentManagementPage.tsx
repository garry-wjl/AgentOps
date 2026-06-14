import { PlusOutlined } from '@ant-design/icons';
import { Button, Input, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useSpaceResourceStore } from '@/stores/spaceResourceStore';
import type { AgentItem, AgentStatus } from '@/mock/agents';
import { ellipsisCell } from '@/utils/listCell';

const { Title, Paragraph, Text } = Typography;

const STATUS_LABELS: Record<AgentStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ONLINE: { color: 'green', label: '在线' },
  OFFLINE: { color: 'red', label: '离线' },
};

export default function AgentManagementPage() {
  const navigate = useNavigate();
  const { spaceId = '' } = useParams();
  const agents = useSpaceResourceStore((s) => s.agents);
  const removeAgent = useSpaceResourceStore((s) => s.removeAgent);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<AgentStatus | 'ALL'>('ALL');

  const filtered = useMemo(
    () =>
      agents.filter(
        (a) =>
          (statusFilter === 'ALL' || a.status === statusFilter) &&
          (!keyword || a.name.includes(keyword) || a.num.includes(keyword)),
      ),
    [agents, statusFilter, keyword],
  );

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Agent 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 Agent 的创建、配置、上下线
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索 Agent 名称或编码"
            allowClear
            onSearch={setKeyword}
            style={{ width: 240 }}
          />
          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            style={{ width: 120 }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'ONLINE', label: '在线' },
              { value: 'OFFLINE', label: '离线' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            新建 Agent
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        dataSource={filtered}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1280 }}
        tableLayout="fixed"
        columns={[
          {
            title: '编码',
            dataIndex: 'num',
            width: 240,
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
            width: 240,
            render: (v: string) => ellipsisCell(v),
          },
          {
            title: '模型',
            dataIndex: 'modelName',
            width: 160,
            render: (v: string) => <Tag color="purple">{v}</Tag>,
          },
          { title: 'Prompt Key', dataIndex: 'promptKey', width: 220, render: (v) => ellipsisCell(v) },
          {
            title: 'Skill / 工具 / 沙箱',
            width: 320,
            render: (_: unknown, r: AgentItem) => (
              <Space wrap size={4}>
                {r.skills.map((s) => (
                  <Tag key={s} color="cyan">
                    {s}
                  </Tag>
                ))}
                {r.tools.map((s) => (
                  <Tag key={s} color="gold">
                    {s}
                  </Tag>
                ))}
                {r.sandboxName && <Tag color="blue">沙箱:{r.sandboxName}</Tag>}
              </Space>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: AgentStatus) => (
              <Tag color={STATUS_LABELS[s].color}>{STATUS_LABELS[s].label}</Tag>
            ),
          },
          { title: '最近修改', dataIndex: 'updatedBy', width: 120 },
          { title: '更新时间', dataIndex: 'updatedAt', width: 160 },
          {
            title: '操作',
            width: 200,
            fixed: 'right',
            render: (_: unknown, r: AgentItem) => (
              <Space>
                <a onClick={() => navigate(`${r.id}/edit`)}>查看</a>
                <a onClick={() => navigate(`${r.id}/edit`)}>编辑</a>
                <Popconfirm
                  title={`确定删除 ${r.name}？`}
                  onConfirm={() => {
                    removeAgent(r.id);
                    message.success('已删除');
                  }}
                >
                  <a style={{ color: '#ff4d4f' }}>删除</a>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
    </div>
  );
}
