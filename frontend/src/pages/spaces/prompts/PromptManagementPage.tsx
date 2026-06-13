import { PlusOutlined } from '@ant-design/icons';
import { Button, Drawer, Form, Input, Select, Space, Table, Tag, Typography, message } from 'antd';
import { useMemo, useState } from 'react';
import { mockPrompts, type PromptItem, type PromptStatus } from '@/mock/prompts';

const { Title, Paragraph, Text } = Typography;

const STATUS: Record<PromptStatus, { color: string; label: string }> = {
  DRAFT: { color: 'default', label: '草稿' },
  ENABLED: { color: 'green', label: '启用' },
  DISABLED: { color: 'red', label: '禁用' },
};

/**
 * Prompt 管理 —— 列表 + 双栏 Markdown 编辑器（编辑/预览）。
 */
export default function PromptManagementPage() {
  const [list, setList] = useState<PromptItem[]>(mockPrompts);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<PromptStatus | 'ALL'>('ALL');
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<PromptItem | null>(null);
  const [content, setContent] = useState('');
  const [form] = Form.useForm();

  const filtered = useMemo(
    () =>
      list.filter(
        (p) =>
          (statusFilter === 'ALL' || p.status === statusFilter) &&
          (!keyword ||
            p.name.includes(keyword) ||
            p.promptKey.includes(keyword) ||
            (p.remark || '').includes(keyword)),
      ),
    [list, keyword, statusFilter],
  );

  const variables = useMemo(() => parseVariables(content), [content]);

  function openCreate() {
    setEditing(null);
    setContent('');
    form.resetFields();
    setDrawerOpen(true);
  }

  function openEdit(p: PromptItem) {
    setEditing(p);
    setContent(p.content);
    form.setFieldsValue(p);
    setDrawerOpen(true);
  }

  function handleSubmit(submit: boolean) {
    form.validateFields().then((values) => {
      if (editing) {
        setList((prev) =>
          prev.map((it) =>
            it.id === editing.id
              ? {
                  ...it,
                  ...values,
                  content,
                  variables,
                  status: submit ? 'ENABLED' : it.status,
                  updatedBy: '当前用户',
                  updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
                }
              : it,
          ),
        );
      } else {
        const it: PromptItem = {
          id: `pr-${Date.now()}`,
          num: submit ? `PR${Date.now()}001` : '',
          ...values,
          content,
          variables,
          status: submit ? 'ENABLED' : 'DRAFT',
          updatedBy: '当前用户',
          updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' '),
        };
        setList((prev) => [it, ...prev]);
      }
      message.success(submit ? '已提交，状态：启用' : '草稿已保存');
      setDrawerOpen(false);
    });
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            Prompt 管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            空间内 Prompt 资产 · Markdown 内容支持 <Text code>{'{{变量}}'}</Text> 占位 · 仅启用态可被 Agent 引用
          </Paragraph>
        </div>
        <Space>
          <Input.Search placeholder="搜索名称/Key/备注" allowClear onSearch={setKeyword} style={{ width: 260 }} />
          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            style={{ width: 120 }}
            options={[
              { value: 'ALL', label: '全部状态' },
              { value: 'DRAFT', label: '草稿' },
              { value: 'ENABLED', label: '启用' },
              { value: 'DISABLED', label: '禁用' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新建 Prompt
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        dataSource={filtered}
        pagination={{ pageSize: 10 }}
        columns={[
          {
            title: '名称',
            dataIndex: 'name',
            render: (v, r) => (
              <Space direction="vertical" size={0}>
                <Text strong>{v}</Text>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {r.num || '草稿未生成编码'}
                </Text>
              </Space>
            ),
          },
          { title: 'Key', dataIndex: 'promptKey', width: 240, render: (v) => <Text code>{v}</Text> },
          {
            title: '变量',
            width: 200,
            render: (_, r) => (
              <Space wrap size={4}>
                {r.variables.map((v) => (
                  <Tag key={v} color="cyan">
                    {`{{${v}}}`}
                  </Tag>
                ))}
              </Space>
            ),
          },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s: PromptStatus) => <Tag color={STATUS[s].color}>{STATUS[s].label}</Tag>,
          },
          { title: '最近修改', dataIndex: 'updatedBy', width: 100 },
          { title: '更新时间', dataIndex: 'updatedAt', width: 140 },
          {
            title: '操作',
            width: 200,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>查看</a>
                <a onClick={() => openEdit(r)}>编辑</a>
                {r.status === 'DRAFT' && <a>提交</a>}
                {r.status === 'ENABLED' && <a>禁用</a>}
                {r.status === 'DISABLED' && <a>启用</a>}
              </Space>
            ),
          },
        ]}
      />

      <Drawer
        title={editing ? `编辑 Prompt - ${editing.name}` : '新建 Prompt'}
        width={1080}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            {(!editing || editing.status === 'DRAFT') && (
              <>
                <Button onClick={() => handleSubmit(false)}>保存为草稿</Button>
                <Button type="primary" onClick={() => handleSubmit(true)}>
                  保存并提交
                </Button>
              </>
            )}
            {editing && editing.status !== 'DRAFT' && (
              <Button type="primary" onClick={() => handleSubmit(false)}>
                保存
              </Button>
            )}
          </Space>
        }
      >
        <div className="prompt-editor">
          <div className="prompt-editor-meta">
            <Form form={form} layout="vertical">
              <Form.Item label="业务编码">
                <Input value={editing?.num || '系统提交后生成'} disabled />
              </Form.Item>
              <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                <Input maxLength={50} />
              </Form.Item>
              <Form.Item
                name="promptKey"
                label="Key"
                rules={[
                  { required: true, message: '请输入 Key' },
                  { pattern: /^[A-Za-z0-9_-]{1,64}$/, message: '英文字母/数字/下划线/中划线，1～64 字符' },
                ]}
              >
                <Input
                  placeholder="customer_service_opening"
                  disabled={!!editing && editing.status !== 'DRAFT'}
                />
              </Form.Item>
              <Form.Item label="状态">
                <Tag color={editing ? STATUS[editing.status].color : 'default'}>
                  {editing ? STATUS[editing.status].label : '草稿'}
                </Tag>
              </Form.Item>
              <Form.Item name="remark" label="备注">
                <Input.TextArea rows={2} maxLength={200} />
              </Form.Item>
              <Form.Item label="变量列表（保存后自动解析）">
                <Space wrap size={4}>
                  {variables.length === 0 && <Text type="secondary">暂无</Text>}
                  {variables.map((v) => (
                    <Tag key={v} color="cyan">{`{{${v}}}`}</Tag>
                  ))}
                </Space>
              </Form.Item>
            </Form>
          </div>
          <div className="prompt-editor-main">
            <div className="prompt-editor-pane">
              <div className="pane-title">编辑</div>
              <Input.TextArea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={22}
                placeholder="Markdown 内容，支持 {{变量}} 占位"
              />
            </div>
            <div className="prompt-editor-pane">
              <div className="pane-title">预览</div>
              <pre className="markdown-preview">{highlight(content)}</pre>
            </div>
          </div>
        </div>
      </Drawer>
    </div>
  );
}

function parseVariables(text: string): string[] {
  const set = new Set<string>();
  const re = /\{\{([A-Za-z_][A-Za-z0-9_]{0,31})\}\}/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text))) set.add(m[1]);
  return Array.from(set);
}

function highlight(text: string): React.ReactNode {
  const parts = text.split(/(\{\{[A-Za-z_][A-Za-z0-9_]{0,31}\}\})/g);
  return parts.map((p, i) =>
    /^\{\{[A-Za-z_][A-Za-z0-9_]{0,31}\}\}$/.test(p) ? (
      <span key={i} className="var-token">
        {p}
      </span>
    ) : (
      <span key={i}>{p}</span>
    ),
  );
}
