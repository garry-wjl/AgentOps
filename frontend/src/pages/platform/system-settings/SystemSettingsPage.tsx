import { Tabs, Form, Input, InputNumber, Switch, Button, Radio, Space, Typography, Table, Tag, Drawer, Descriptions, Alert } from 'antd';
import { useState } from 'react';
import {
  mockMailSettings,
  mockPlatformBasic,
  mockSandboxAccess,
  mockSpacePolicy,
  mockAuditLogs,
  type AuditLogItem,
} from '@/mock/settings';

const { Title, Paragraph } = Typography;

/**
 * 系统设置：左侧二级菜单 + 右侧分组配置表单。
 * 分组：平台基础信息 / 邮件服务 / 空间策略 / 沙箱接入 / 审计日志（V1.2）。
 */
export default function SystemSettingsPage() {
  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            系统设置
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            仅平台管理员可访问 · 配置项保存即生效
          </Paragraph>
        </div>
      </div>

      <Tabs
        tabPosition="left"
        items={[
          { key: 'basic', label: '平台基础信息', children: <BasicForm /> },
          { key: 'mail', label: '邮件服务', children: <MailForm /> },
          { key: 'space', label: '空间策略', children: <SpacePolicyForm /> },
          { key: 'sandbox', label: '沙箱接入', children: <SandboxAccessForm /> },
          { key: 'audit', label: '审计日志', children: <AuditList /> },
        ]}
        className="settings-tabs"
      />
    </div>
  );
}

function BasicForm() {
  const [form] = Form.useForm();
  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={mockPlatformBasic}
      style={{ maxWidth: 640 }}
    >
      <Form.Item name="platformName" label="平台名称" rules={[{ required: true }]}>
        <Input maxLength={30} />
      </Form.Item>
      <Form.Item label="平台 Logo">
        <div className="logo-uploader">
          <div className="logo-preview">A</div>
          <Space>
            <Button>上传新 Logo</Button>
            <Button>恢复默认</Button>
          </Space>
        </div>
        <div className="form-hint">支持 PNG/JPG/SVG，≤ 2MB，建议 256×256</div>
      </Form.Item>
      <Form.Item name="icpNumber" label="ICP 备案号">
        <Input maxLength={50} />
      </Form.Item>
      <Form.Item name="supportEmail" label="技术支持邮箱">
        <Input />
      </Form.Item>
      <Form.Item name="supportPhone" label="技术支持电话">
        <Input maxLength={20} />
      </Form.Item>
      <Space>
        <Button>重置</Button>
        <Button type="primary">保存</Button>
      </Space>
    </Form>
  );
}

function MailForm() {
  const [enabled, setEnabled] = useState(mockMailSettings.enabled);
  return (
    <Form
      layout="vertical"
      initialValues={mockMailSettings}
      style={{ maxWidth: 640 }}
    >
      <Form.Item name="enabled" label="启用 SMTP" valuePropName="checked">
        <Switch checkedChildren="启用" unCheckedChildren="关闭" onChange={setEnabled} />
      </Form.Item>
      <Form.Item name="host" label="SMTP 主机" rules={enabled ? [{ required: true }] : []}>
        <Input disabled={!enabled} />
      </Form.Item>
      <Form.Item name="port" label="SMTP 端口" rules={enabled ? [{ required: true }] : []}>
        <InputNumber min={1} max={65535} disabled={!enabled} style={{ width: 200 }} />
      </Form.Item>
      <Form.Item name="encryption" label="加密方式">
        <Radio.Group disabled={!enabled}>
          <Radio value="NONE">无</Radio>
          <Radio value="SSL">SSL</Radio>
          <Radio value="STARTTLS">STARTTLS</Radio>
        </Radio.Group>
      </Form.Item>
      <Form.Item name="account" label="发件账号" rules={enabled ? [{ required: true }] : []}>
        <Input disabled={!enabled} />
      </Form.Item>
      <Form.Item label="发件密码">
        <Input.Password disabled={!enabled} placeholder={mockMailSettings.passwordConfigured ? '****（已配置）' : '未配置'} />
      </Form.Item>
      <Form.Item name="fromName" label="发件人显示名">
        <Input disabled={!enabled} />
      </Form.Item>
      <Space>
        <Button disabled={!enabled}>发送测试邮件</Button>
        <Button>重置</Button>
        <Button type="primary">保存</Button>
      </Space>
    </Form>
  );
}

function SpacePolicyForm() {
  return (
    <Form
      layout="vertical"
      initialValues={mockSpacePolicy}
      style={{ maxWidth: 640 }}
    >
      <Form.Item name="maxSpacesPerUser" label="单用户最大可创建空间数" rules={[{ required: true }]}>
        <InputNumber min={1} max={100} style={{ width: 200 }} />
      </Form.Item>
      <Form.Item name="spaceNameMinLen" label="空间名称最小长度">
        <InputNumber min={1} max={50} style={{ width: 200 }} />
      </Form.Item>
      <Form.Item name="spaceNameMaxLen" label="空间名称最大长度">
        <InputNumber min={1} max={50} style={{ width: 200 }} />
      </Form.Item>
      <Form.Item name="charset" label="空间名称字符集合">
        <Radio.Group>
          <Radio value="EN_NUM">仅英文+数字</Radio>
          <Radio value="CN_EN_NUM">中英文+数字</Radio>
          <Radio value="CN_EN_NUM_SYM">中英文+数字+常见符号</Radio>
        </Radio.Group>
      </Form.Item>
      <Alert
        showIcon
        type="info"
        message="该策略仅作用于新建/改名空间，不回溯存量空间"
        style={{ marginBottom: 16 }}
      />
      <Space>
        <Button>重置</Button>
        <Button type="primary">保存</Button>
      </Space>
    </Form>
  );
}

function SandboxAccessForm() {
  const [openSb, setOpenSb] = useState(mockSandboxAccess.openSandboxEnabled);
  const [aliyun, setAliyun] = useState(mockSandboxAccess.aliyunEnabled);
  return (
    <Form
      layout="vertical"
      initialValues={mockSandboxAccess}
      style={{ maxWidth: 720 }}
    >
      <Title level={5}>OpenSandbox</Title>
      <Form.Item name="openSandboxEnabled" label="启用" valuePropName="checked">
        <Switch onChange={setOpenSb} checkedChildren="启用" unCheckedChildren="关闭" />
      </Form.Item>
      <Form.Item name="openSandboxEndpoint" label="接入地址" rules={openSb ? [{ required: true }] : []}>
        <Input disabled={!openSb} placeholder="https://opensandbox.internal:8080" />
      </Form.Item>
      <Form.Item label="API Token">
        <Input.Password disabled={!openSb} placeholder={mockSandboxAccess.openSandboxTokenConfigured ? '****（已配置）' : '未配置'} />
      </Form.Item>
      <Button disabled={!openSb} style={{ marginBottom: 24 }}>
        测试连接
      </Button>

      <Title level={5}>阿里云沙箱</Title>
      <Form.Item name="aliyunEnabled" label="启用" valuePropName="checked">
        <Switch onChange={setAliyun} checkedChildren="启用" unCheckedChildren="关闭" />
      </Form.Item>
      <Form.Item name="aliyunEndpoint" label="接入地址">
        <Input disabled={!aliyun} />
      </Form.Item>
      <Form.Item label="AccessKey ID">
        <Input disabled={!aliyun} placeholder="LTAI************" />
      </Form.Item>
      <Form.Item label="AccessKey Secret">
        <Input.Password disabled={!aliyun} placeholder={mockSandboxAccess.aliyunAkConfigured ? '****（已配置）' : '未配置'} />
      </Form.Item>
      <Form.Item name="aliyunRegion" label="Region">
        <Input disabled={!aliyun} />
      </Form.Item>
      <Button disabled={!aliyun} style={{ marginBottom: 24 }}>
        测试连接
      </Button>

      <Title level={5}>探活参数（对所有供应商生效）</Title>
      <Form.Item name="probeIntervalSec" label="探活间隔（秒）">
        <InputNumber min={30} max={600} style={{ width: 200 }} />
      </Form.Item>
      <Form.Item name="probeTimeoutSec" label="单次探活超时（秒）">
        <InputNumber min={1} max={30} style={{ width: 200 }} />
      </Form.Item>
      <Form.Item name="firstStartTimeoutSec" label="首次启动判定超时（秒）">
        <InputNumber min={30} max={600} style={{ width: 200 }} />
      </Form.Item>

      <Space>
        <Button>重置</Button>
        <Button type="primary">保存</Button>
      </Space>
    </Form>
  );
}

function AuditList() {
  const [detail, setDetail] = useState<AuditLogItem | null>(null);
  return (
    <>
      <Space style={{ marginBottom: 16 }} wrap>
        <Input placeholder="操作人" style={{ width: 160 }} />
        <Input placeholder="资源类型" style={{ width: 140 }} />
        <Input placeholder="操作类型" style={{ width: 140 }} />
        <Input placeholder="关键字" style={{ width: 200 }} />
        <Button type="primary">查询</Button>
      </Space>
      <Table
        rowKey="id"
        dataSource={mockAuditLogs}
        pagination={{ pageSize: 20 }}
        columns={[
          { title: '时间', dataIndex: 'occurredAt', width: 160 },
          { title: '操作人', dataIndex: 'operator', width: 160 },
          {
            title: '资源类型',
            dataIndex: 'resourceType',
            width: 110,
            render: (v) => <Tag>{v}</Tag>,
          },
          { title: '操作', dataIndex: 'action', width: 160 },
          { title: '资源编码', dataIndex: 'resourceNum' },
          { title: 'IP', dataIndex: 'ip', width: 120 },
          {
            title: '详情',
            width: 80,
            render: (_, r) => (
              <a onClick={() => setDetail(r)}>查看</a>
            ),
          },
        ]}
      />
      <Drawer
        title="审计日志详情"
        width={520}
        open={!!detail}
        onClose={() => setDetail(null)}
      >
        {detail && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="操作时间">{detail.occurredAt}</Descriptions.Item>
            <Descriptions.Item label="操作人">{detail.operator}</Descriptions.Item>
            <Descriptions.Item label="资源类型">{detail.resourceType}</Descriptions.Item>
            <Descriptions.Item label="资源编码">{detail.resourceNum}</Descriptions.Item>
            <Descriptions.Item label="操作类型">{detail.action}</Descriptions.Item>
            <Descriptions.Item label="IP">{detail.ip}</Descriptions.Item>
            <Descriptions.Item label="客户端 UA">{detail.ua}</Descriptions.Item>
            <Descriptions.Item label="变更摘要">{detail.changeSummary}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </>
  );
}
