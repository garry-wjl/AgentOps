import {
  ApartmentOutlined,
  ApiOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
  FileTextOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import { Card, Col, Row, Statistic, Tag, Timeline, Typography } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { findSpace, mockSpaces } from '@/mock/spaces';
import { mockAgents } from '@/mock/agents';
import { mockSandboxes } from '@/mock/sandboxes';
import { mockModels } from '@/mock/models';
import { mockPrompts } from '@/mock/prompts';
import { mockSkills } from '@/mock/skills';
import { mockTools } from '@/mock/tools';

const { Title, Paragraph, Text } = Typography;

/**
 * 空间工作台 —— 空间默认页（统计卡片 + 快捷入口 + 最近动态）。
 */
export default function SpaceWorkbenchPage() {
  const { spaceId = '' } = useParams();
  const navigate = useNavigate();
  const space = findSpace(spaceId) ?? mockSpaces[0];

  const stats = [
    { title: 'Agent 数量', value: mockAgents.length, icon: <RobotOutlined />, key: 'agents' },
    { title: '在线沙箱', value: mockSandboxes.filter((s) => s.status === 'ONLINE').length, icon: <CodeOutlined />, key: 'sandboxes' },
    { title: '可用模型', value: mockModels.filter((m) => m.status === 'ENABLED').length, icon: <ApiOutlined />, key: 'models' },
    { title: '启用 Prompt', value: mockPrompts.filter((p) => p.status === 'ENABLED').length, icon: <FileTextOutlined />, key: 'prompts' },
    { title: 'Skill 数量', value: mockSkills.length, icon: <ThunderboltOutlined />, key: 'skills' },
    { title: '工具数量', value: mockTools.length, icon: <ToolOutlined />, key: 'tools' },
  ];

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            <ApartmentOutlined /> {space.name}
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            {space.num} · 创建人 {space.createdBy} · 你的角色：
            <Tag color={space.myRole === 'ADMIN' ? 'blue' : 'green'} style={{ marginLeft: 4 }}>
              {space.myRole === 'ADMIN' ? '管理员' : '普通成员'}
            </Tag>
          </Paragraph>
        </div>
      </div>

      <Row gutter={16}>
        {stats.map((s) => (
          <Col span={4} key={s.key}>
            <Card
              hoverable
              onClick={() => navigate(`/spaces/${spaceId}/${s.key}`)}
              className="dashboard-stat-card"
            >
              <Statistic title={<><span style={{ marginRight: 6 }}>{s.icon}</span>{s.title}</>} value={s.value} />
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={14}>
          <Card title="最近动态" variant="outlined">
            <Timeline
              items={[
                {
                  color: 'green',
                  children: (
                    <>
                      <Text strong>客服总入口</Text> 由 张三 部署上线 · 2026-06-13 15:42
                    </>
                  ),
                },
                {
                  color: 'blue',
                  children: (
                    <>
                      沙箱 <Text strong>数据分析-A</Text> 探活成功，状态恢复在线 · 2026-06-13 15:30
                    </>
                  ),
                },
                {
                  color: 'orange',
                  children: (
                    <>
                      沙箱 <Text strong>报表沙箱</Text> 探活超时，状态转为离线 · 2026-06-13 15:30
                    </>
                  ),
                },
                {
                  children: (
                    <>
                      Prompt <Text strong>客服-开场白</Text> 由 李四 编辑 · 2026-06-13 14:01
                    </>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
        <Col span={10}>
          <Card title="空间健康概览" variant="outlined">
            <p>
              <CheckCircleOutlined style={{ color: '#52c41a' }} /> 模型连通性：3/4 健康
            </p>
            <p>
              <CheckCircleOutlined style={{ color: '#52c41a' }} /> Agent 在线率：2/4
            </p>
            <p>
              <ClockCircleOutlined style={{ color: '#faad14' }} /> 沙箱在线率：1/3（不含禁用）
            </p>
            <p>
              <ApartmentOutlined /> 成员数量：{space.admins.length + space.members.length}
            </p>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
