import {
  ApartmentOutlined,
  ApiOutlined,
  ArrowRightOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
  ExclamationCircleOutlined,
  FileTextOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  ToolOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Badge, Button, Card, Col, Empty, List, Row, Space, Statistic, Tag, Timeline, Tooltip, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { mockSpaces } from '@/mock/spaces';
import { mockAgents } from '@/mock/agents';
import { mockSandboxes } from '@/mock/sandboxes';
import { mockModels } from '@/mock/models';
import { mockPrompts } from '@/mock/prompts';
import { mockSkills } from '@/mock/skills';
import { mockTools } from '@/mock/tools';

const { Title, Paragraph, Text } = Typography;

/**
 * 平台工作台 —— 登录后默认落地页。
 * 跨空间总览：欢迎语、全局统计、我参与的空间、跨空间最近动态、健康/告警。
 */
export default function PlatformWorkbenchPage() {
  const navigate = useNavigate();
  const currentUser = useAuthStore((s) => s.currentUser);

  const spaceCount = mockSpaces.length;
  const adminSpaceCount = mockSpaces.filter((s) => s.myRole === 'ADMIN').length;
  const onlineAgents = mockAgents.filter((a) => a.status === 'ONLINE').length;
  const onlineSandboxes = mockSandboxes.filter((s) => s.status === 'ONLINE').length;
  const offlineSandboxes = mockSandboxes.filter((s) => s.status === 'OFFLINE').length;
  const enabledModels = mockModels.filter((m) => m.status === 'ENABLED').length;
  const degradedModels = mockModels.filter((m) => m.health === 'DEGRADED').length;
  const enabledPrompts = mockPrompts.filter((p) => p.status === 'ENABLED').length;
  const enabledSkills = mockSkills.filter((s) => s.status === 'ENABLED').length;
  const enabledTools = mockTools.filter((t) => t.status === 'ENABLED').length;

  const greeting = getGreeting();

  return (
    <div className="page-section workbench-page">
      {/* 欢迎区 */}
      <div className="workbench-hero">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {greeting}，{currentUser?.name || '同学'} 👋
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            你共参与 <Text strong>{spaceCount}</Text> 个空间，其中 <Text strong>{adminSpaceCount}</Text> 个为管理员
          </Paragraph>
        </div>
      </div>

      {/* 跨空间统计卡 */}
      <Row gutter={[16, 16]} className="workbench-stats">
        <Col xs={12} sm={8} lg={4}>
          <Card variant="outlined" hoverable onClick={() => navigate('/platform/spaces')}>
            <Statistic
              title={
                <span>
                  <ApartmentOutlined /> 我的空间
                </span>
              }
              value={spaceCount}
              suffix={<Text type="secondary" style={{ fontSize: 12 }}>{adminSpaceCount} 管理</Text>}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <StatCard icon={<RobotOutlined />} label="在线 Agent" value={onlineAgents} total={mockAgents.length} />
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <StatCard
            icon={<CodeOutlined />}
            label="在线沙箱"
            value={onlineSandboxes}
            total={mockSandboxes.filter((s) => s.status !== 'DRAFT').length}
            warn={offlineSandboxes > 0 ? `${offlineSandboxes} 离线` : undefined}
          />
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <StatCard
            icon={<ApiOutlined />}
            label="可用模型"
            value={enabledModels}
            total={mockModels.length}
            warn={degradedModels > 0 ? `${degradedModels} 降级` : undefined}
          />
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <StatCard icon={<FileTextOutlined />} label="启用 Prompt" value={enabledPrompts} total={mockPrompts.length} />
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <StatCard icon={<ThunderboltOutlined />} label="启用 Skill" value={enabledSkills} total={mockSkills.length} />
        </Col>
      </Row>

      {/* 我参与的空间 + 健康概览 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={14}>
          <Card
            variant="outlined"
            className="workbench-card"
            title="我参与的空间"
            extra={<a onClick={() => navigate('/platform/spaces')}>查看全部 <ArrowRightOutlined /></a>}
          >
            {mockSpaces.length === 0 ? (
              <Empty description="还没有空间，去创建一个" />
            ) : (
              <List
                dataSource={mockSpaces}
                renderItem={(s) => (
                  <List.Item
                    className="workbench-space-row"
                    actions={[
                      <Button key="enter" type="link" onClick={() => navigate(`/spaces/${s.id}/agents`)}>
                        进入 <ArrowRightOutlined />
                      </Button>,
                    ]}
                  >
                    <List.Item.Meta
                      avatar={
                        <Avatar size={40} className="workbench-space-avatar">
                          {s.name.charAt(0)}
                        </Avatar>
                      }
                      title={
                        <Space>
                          <Text strong>{s.name}</Text>
                          <Tag color={s.myRole === 'ADMIN' ? 'blue' : 'green'} bordered={false}>
                            {s.myRole === 'ADMIN' ? '管理' : '成员'}
                          </Tag>
                        </Space>
                      }
                      description={
                        <Space split="·" wrap>
                          <Text type="secondary" style={{ fontSize: 12 }}>{s.num}</Text>
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            <UserOutlined /> {s.createdBy}
                          </Text>
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            {s.admins.length} 管理 / {s.members.length} 成员
                          </Text>
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card variant="outlined" className="workbench-card" title="平台健康">
            <Space direction="vertical" size={12} style={{ width: '100%' }}>
              <HealthRow
                label="模型连通性"
                value={`${enabledModels - degradedModels}/${mockModels.length}`}
                ok={degradedModels === 0}
                hint={degradedModels > 0 ? `${degradedModels} 个模型降级，建议检查` : '全部健康'}
              />
              <HealthRow
                label="Agent 在线率"
                value={`${onlineAgents}/${mockAgents.length}`}
                ok={onlineAgents > 0}
                hint={
                  mockAgents.length === 0
                    ? '尚无 Agent'
                    : `${mockAgents.length - onlineAgents} 个非在线（含草稿/离线）`
                }
              />
              <HealthRow
                label="沙箱在线率"
                value={`${onlineSandboxes}/${mockSandboxes.filter((s) => s.status !== 'DRAFT' && s.status !== 'DISABLED').length}`}
                ok={offlineSandboxes === 0}
                hint={offlineSandboxes > 0 ? `${offlineSandboxes} 个沙箱探活失败` : '全部在线'}
              />
              <HealthRow
                label="资产丰富度"
                value={`${enabledPrompts}P / ${enabledSkills}S / ${enabledTools}T`}
                ok
                hint="启用的 Prompt / Skill / 工具数量"
              />
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 跨空间最近动态 + 快捷入口 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={14}>
          <Card variant="outlined" className="workbench-card" title="最近动态（跨空间）">
            <Timeline
              items={[
                {
                  color: 'green',
                  children: (
                    <>
                      <Text strong>家庭客服 Agent</Text> · Agent <Text strong>客服总入口</Text> 部署上线 ·
                      <Text type="secondary"> 张三</Text> · 2026-06-13 15:42
                    </>
                  ),
                },
                {
                  color: 'blue',
                  children: (
                    <>
                      <Text strong>家庭客服 Agent</Text> · 沙箱 <Text strong>数据分析-A</Text> 探活成功，恢复在线 · 2026-06-13 15:30
                    </>
                  ),
                },
                {
                  color: 'orange',
                  children: (
                    <>
                      <Text strong>数据分析实验</Text> · 沙箱 <Text strong>报表沙箱</Text> 探活超时，状态转为离线 · 2026-06-13 15:30
                    </>
                  ),
                },
                {
                  children: (
                    <>
                      <Text strong>办公自动化空间</Text> · Prompt <Text strong>FAQ-保险类</Text> 新版本由 李四 提交 · 2026-06-13 14:01
                    </>
                  ),
                },
                {
                  children: (
                    <>
                      <Text strong>家庭客服 Agent</Text> · 模型 <Text strong>Azure GPT-4o-mini</Text> 健康降级 · 2026-06-12 15:05
                    </>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card variant="outlined" className="workbench-card" title="快捷入口">
            <div className="workbench-shortcuts">
              <ShortcutTile
                icon={<ApartmentOutlined />}
                label="空间管理"
                hint="管理你参与的全部空间"
                onClick={() => navigate('/platform/spaces')}
              />
              <ShortcutTile
                icon={<RobotOutlined />}
                label="进入第一个空间"
                hint={mockSpaces[0]?.name}
                onClick={() => navigate(`/spaces/${mockSpaces[0]?.id}/agents`)}
                disabled={mockSpaces.length === 0}
              />
              <ShortcutTile
                icon={<UserOutlined />}
                label="用户管理"
                hint="仅管理员可见"
                onClick={() => navigate('/platform/users')}
              />
              <ShortcutTile
                icon={<ToolOutlined />}
                label="系统设置"
                hint="平台基础信息 / 邮件 / 沙箱接入"
                onClick={() => navigate('/platform/system-settings')}
              />
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

interface StatCardProps {
  icon: React.ReactNode;
  label: string;
  value: number;
  total?: number;
  warn?: string;
}
function StatCard({ icon, label, value, total, warn }: StatCardProps) {
  return (
    <Card variant="outlined" hoverable>
      <Statistic
        title={
          <span>
            {icon} {label}
          </span>
        }
        value={value}
        suffix={
          total !== undefined ? (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {' / '}
              {total}
            </Text>
          ) : undefined
        }
      />
      {warn && (
        <Tooltip title={warn}>
          <Tag color="warning" icon={<ExclamationCircleOutlined />} style={{ marginTop: 4 }}>
            {warn}
          </Tag>
        </Tooltip>
      )}
    </Card>
  );
}

interface HealthRowProps {
  label: string;
  value: string;
  ok: boolean;
  hint?: string;
}
function HealthRow({ label, value, ok, hint }: HealthRowProps) {
  return (
    <div className="workbench-health-row">
      <Space>
        {ok ? (
          <CheckCircleOutlined style={{ color: '#52c41a' }} />
        ) : (
          <ClockCircleOutlined style={{ color: '#faad14' }} />
        )}
        <Text>{label}</Text>
      </Space>
      <Space size={12}>
        <Text strong>{value}</Text>
        {hint && (
          <Text type="secondary" style={{ fontSize: 12 }}>
            {hint}
          </Text>
        )}
      </Space>
    </div>
  );
}

interface ShortcutTileProps {
  icon: React.ReactNode;
  label: string;
  hint?: string;
  onClick: () => void;
  disabled?: boolean;
}
function ShortcutTile({ icon, label, hint, onClick, disabled }: ShortcutTileProps) {
  return (
    <button
      type="button"
      className={`workbench-shortcut-tile${disabled ? ' is-disabled' : ''}`}
      onClick={() => !disabled && onClick()}
      disabled={disabled}
    >
      <span className="workbench-shortcut-icon">{icon}</span>
      <span>
        <span className="workbench-shortcut-label">{label}</span>
        {hint && <span className="workbench-shortcut-hint">{hint}</span>}
      </span>
      <ArrowRightOutlined className="workbench-shortcut-arrow" />
    </button>
  );
}

function getGreeting(): string {
  const h = new Date().getHours();
  if (h < 6) return '凌晨好';
  if (h < 9) return '早上好';
  if (h < 12) return '上午好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  if (h < 23) return '晚上好';
  return '夜深了';
}
