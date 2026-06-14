import { PlusOutlined } from '@ant-design/icons';
import { Badge, Button, Input, Popconfirm, Space, Table, Tag, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSpaceResourceStore } from '@/stores/spaceResourceStore';
import type { ToolItem, ToolStatus, ToolType } from '@/mock/tools';
import { ellipsisCell } from '@/utils/listCell';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<ToolStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

const HEALTH = {
  HEALTHY: { color: 'success', label: '健康' },
  DEGRADED: { color: 'warning', label: '降级' },
  UNKNOWN: { color: 'default', label: '未知' },
} as const;

export default function ToolManagementPage() {
  const navigate = useNavigate();
  const tools = useSpaceResourceStore((s) => s.tools);
  const removeTool = useSpaceResourceStore((s) => s.removeTool);
  const [keyword, setKeyword] = useState('');

  const filtered = useMemo(
    () => tools.filter((t) => !keyword || t.name.includes(keyword) || t.num.includes(keyword)),
    [tools, keyword],
  );

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            工具管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            MCP 协议工具与 Function Call 工具的注册、测试
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索名称或编码"
            allowClear
            onSearch={setKeyword}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            注册工具
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
          { title: 'Key', dataIndex: 'toolKey', width: 200, render: (v: string) => <Text code>{v}</Text> },
          {
            title: '类型',
            dataIndex: 'type',
            width: 110,
            render: (t: ToolType) => <Tag color={t === 'MCP' ? 'geekblue' : 'gold'}>{t}</Tag>,
          },
          { title: 'Endpoint', dataIndex: 'endpoint', width: 280, render: (v: string) => ellipsisCell(v) },
          { title: '认证', dataIndex: 'authType', width: 90, render: (v: string) => <Tag>{v}</Tag> },
          {
            title: '健康',
            dataIndex: 'health',
            width: 90,
            render: (h: ToolItem['health']) => (
              <Badge status={HEALTH[h].color as any} text={HEALTH[h].label} />
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: ToolStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          {
            title: '操作',
            width: 200,
            fixed: 'right',
            render: (_: unknown, r: ToolItem) => (
              <Space>
                <a onClick={() => navigate(`${r.id}/edit`)}>查看</a>
                <a onClick={() => navigate(`${r.id}/edit`)}>编辑</a>
                <a>测试调用</a>
                <Popconfirm
                  title={`确定删除 ${r.name}？`}
                  onConfirm={() => {
                    removeTool(r.id);
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
