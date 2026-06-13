import { PageContainer, ProCard } from '@ant-design/pro-components';
import { Form, InputNumber, Select, Switch } from 'antd';

export default function SystemSettingsPage() {
  return (
    <PageContainer title="系统设置" subTitle="仅管理员角色用户可访问">
      <ProCard title="登录安全策略" bordered>
        <Form
          layout="vertical"
          style={{ maxWidth: 640 }}
          initialValues={{ passwordLength: 8, audit: true, theme: 'default' }}
        >
          <Form.Item label="最小密码长度" name="passwordLength">
            <InputNumber min={8} max={32} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="登录失败安全日志" name="audit" valuePropName="checked">
            <Switch checkedChildren="开启" unCheckedChildren="关闭" />
          </Form.Item>
          <Form.Item label="默认主题" name="theme">
            <Select
              options={[
                { label: '默认主题', value: 'default' },
                { label: '紧凑主题', value: 'compact' },
                { label: '暗色主题', value: 'dark' },
              ]}
            />
          </Form.Item>
        </Form>
      </ProCard>
    </PageContainer>
  );
}
