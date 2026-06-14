import { PlusOutlined } from '@ant-design/icons';
import { Button, Input, Popconfirm, Space, Table, Tag, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSpaceResourceStore } from '@/stores/spaceResourceStore';
import type { SkillItem, SkillStatus } from '@/mock/skills';
import { ellipsisCell } from '@/utils/listCell';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<SkillStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

export default function SkillManagementPage() {
  const navigate = useNavigate();
  const skills = useSpaceResourceStore((s) => s.skills);
  const removeSkill = useSpaceResourceStore((s) => s.removeSkill);
  const [keyword, setKeyword] = useState('');

  const filtered = useMemo(
    () => skills.filter((s) => !keyword || s.name.includes(keyword) || s.num.includes(keyword)),
    [skills, keyword],
  );

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Skill 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            Skill 定义、注册中心与 Agent 绑定关系
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
            新建 Skill
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        dataSource={filtered}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1200 }}
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
          { title: 'Key', dataIndex: 'skillKey', width: 220, render: (v: string) => <Text code>{v}</Text> },
          {
            title: '描述',
            dataIndex: 'description',
            width: 280,
            render: (v: string) => ellipsisCell(v),
          },
          {
            title: '已绑定 Agent',
            dataIndex: 'boundAgents',
            width: 200,
            render: (agents: string[]) =>
              agents.length === 0 ? (
                <Text type="secondary">未绑定</Text>
              ) : (
                <Space wrap size={4}>
                  {agents.map((a) => (
                    <Tag key={a} color="blue">
                      {a}
                    </Tag>
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
          {
            title: '操作',
            width: 200,
            fixed: 'right',
            render: (_: unknown, r: SkillItem) => (
              <Space>
                <a onClick={() => navigate(`${r.id}/edit`)}>查看</a>
                <a onClick={() => navigate(`${r.id}/edit`)}>编辑</a>
                <Popconfirm
                  title={`确定删除 ${r.name}？`}
                  onConfirm={() => {
                    removeSkill(r.id);
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
