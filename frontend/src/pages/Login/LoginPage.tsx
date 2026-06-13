import { ApiOutlined, CloudServerOutlined, LockOutlined, MobileOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormCheckbox, ProFormText } from '@ant-design/pro-components';
import { Alert, Card, Space, Typography, message } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

const loginHighlights = [
  { icon: <CloudServerOutlined />, title: '统一 AgentOps 控制台', desc: '空间、模型、Agent、工具资产集中管理' },
  { icon: <SafetyCertificateOutlined />, title: '企业级账号治理', desc: '内置角色、状态流转、操作审计' },
  { icon: <ApiOutlined />, title: '运行时可观测', desc: '面向 Agent 执行链路的监控与追踪' },
];

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((s) => s.login);

  return (
    <div className="login-page volc-login-page">
      <div className="volc-login-shell">
        <section className="volc-login-hero">
          <div className="volc-brand-mark">AgentOps</div>
          <Typography.Title level={1} className="volc-hero-title">
            一体化 Agent 管理平台
          </Typography.Title>
          <Typography.Paragraph className="volc-hero-desc">
            以空间为资源容器，统一管理模型、Agent、Prompt、Skill 与工具，构建企业级 AI Agent 运营控制台。
          </Typography.Paragraph>

          <div className="volc-highlight-list">
            {loginHighlights.map((item) => (
              <div className="volc-highlight-card" key={item.title}>
                <div className="volc-highlight-icon">{item.icon}</div>
                <div>
                  <div className="volc-highlight-title">{item.title}</div>
                  <div className="volc-highlight-desc">{item.desc}</div>
                </div>
              </div>
            ))}
          </div>

          <div className="volc-hero-metrics">
            <div>
              <strong>99.9%</strong>
              <span>平台可用性目标</span>
            </div>
            <div>
              <strong>RBAC</strong>
              <span>角色权限控制</span>
            </div>
            <div>
              <strong>Trace</strong>
              <span>全链路审计</span>
            </div>
          </div>
        </section>

        <Card className="login-card volc-login-card" variant="borderless">
          <div className="login-brand volc-form-brand">
            <div className="login-brand-title">欢迎登录</div>
            <div className="login-brand-subtitle">AgentOps 企业控制台</div>
          </div>

          <LoginForm
            title={false as unknown as string}
            subTitle={false as unknown as string}
            submitter={{
              searchConfig: { submitText: '登录' },
              submitButtonProps: { size: 'large', block: true },
            }}
            onFinish={async (values) => {
              const account = String(values.account || '').trim();
              const password = String(values.password || '');
              if (!account) {
                message.error('请输入邮箱或手机号');
                return false;
              }
              if (!password) {
                message.error('请输入密码');
                return false;
              }
              try {
                await login(account, password);
                message.success('登录成功');
                const from = (location.state as { from?: string } | null)?.from || '/platform/spaces';
                navigate(from, { replace: true });
                return true;
              } catch (error) {
                const err = error as Error & { code?: string };
                message.error(err.message || '账号或密码错误');
                return false;
              }
            }}
          >
            <Alert
              showIcon
              type="info"
              className="volc-login-alert"
              message={
                <Space direction="vertical" size={2}>
                  <Typography.Text>管理员示例：admin@example.com</Typography.Text>
                  <Typography.Text>普通用户示例：user@example.com</Typography.Text>
                </Space>
              }
            />
            <ProFormText
              name="account"
              label="登录账号"
              fieldProps={{ size: 'large', prefix: <UserOutlined />, placeholder: '请输入邮箱或手机号' }}
              rules={[{ required: true, message: '请输入邮箱或手机号' }]}
            />
            <ProFormText.Password
              name="password"
              label="登录密码"
              fieldProps={{ size: 'large', prefix: <LockOutlined />, placeholder: '请输入密码' }}
              rules={[{ required: true, message: '请输入密码' }]}
            />
            <div className="volc-login-extra">
              <ProFormCheckbox name="remember">
                <Space size={4}>
                  <MobileOutlined /> 记住账号
                </Space>
              </ProFormCheckbox>
              <Typography.Link>忘记密码</Typography.Link>
            </div>
          </LoginForm>
        </Card>
      </div>
    </div>
  );
}
