import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { Alert, Space, Tag } from 'antd';
import { hasAdminRole, useAuthStore } from '@/stores/authStore';

export default function SpaceHomePage() {
  const currentUser = useAuthStore((s) => s.currentUser);
  if (!currentUser) {
    return null;
  }
  const admin = hasAdminRole(currentUser);

  return (
    <PageContainer
      title="空间管理"
      subTitle="普通用户和管理员均可访问有权限的空间及空间内资源"
      tags={currentUser.roles.map((role) => (
        <Tag key={role.code} color={role.code === 'ADMIN' ? 'blue' : 'green'}>
          {role.label}
        </Tag>
      ))}
    >
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Alert
          showIcon
          type={admin ? 'success' : 'info'}
          message={
            admin
              ? '当前用户为管理员，可访问空间管理、用户管理与系统设置。'
              : '当前用户为普通用户，仅展示空间管理相关功能，用户管理和系统设置不可访问。'
          }
        />
        <StatisticCard.Group direction="row">
          <StatisticCard statistic={{ title: '可访问空间', value: 3 }} />
          <StatisticCard statistic={{ title: '空间内 Agent', value: 18 }} />
          <StatisticCard statistic={{ title: '空间内模型', value: 6 }} />
          <StatisticCard statistic={{ title: '今日执行次数', value: 1280 }} />
        </StatisticCard.Group>
        <ProCard title="空间内资源" split="vertical">
          <ProCard title="模型管理" className="permission-card">
            <div className="mock-placeholder">空间内模型、供应商、API Key 和参数预设</div>
          </ProCard>
          <ProCard title="Agent 管理" className="permission-card">
            <div className="mock-placeholder">空间内 Agent 创建、运行和监控</div>
          </ProCard>
          <ProCard title="Prompt / Skill / 工具" className="permission-card">
            <div className="mock-placeholder">空间内资产统一管理</div>
          </ProCard>
        </ProCard>
      </Space>
    </PageContainer>
  );
}
