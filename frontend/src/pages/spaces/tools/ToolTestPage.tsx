import { Alert, Button, Card, Form, Input, Skeleton, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getTool, testTool, type ToolDTO, type TestResultDTO } from '@/api/tool';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

/**
 * 工具试运行页 —— 整页表单 + 响应展示。
 *
 * 路由：/spaces/:spaceId/tools/:toolNum/test
 */
export default function ToolTestPage() {
  const navigate = useNavigate();
  const { spaceId = '', toolNum = '' } = useParams();
  const listPath = `/spaces/${spaceId}/tools`;

  const [tool, setTool] = useState<ToolDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState<TestResultDTO | null>(null);
  const [form] = Form.useForm<{ testInput: string }>();

  useEffect(() => {
    if (!toolNum) return;
    setLoading(true);
    getTool(toolNum)
      .then((d) => {
        setTool(d);
        // 默认填一个 JSON 样例（FunctionCall 场景）
        form.setFieldsValue({ testInput: '{\n  "path": "/",\n  "query": {}\n}' });
      })
      .catch((err) => notifyError(err, '加载工具失败'))
      .finally(() => setLoading(false));
  }, [toolNum, form]);

  async function handleRun() {
    let values: { testInput: string };
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    let parsed: Record<string, unknown> | undefined;
    try {
      parsed = values.testInput?.trim() ? JSON.parse(values.testInput) : undefined;
    } catch {
      message.error('试运行入参不是合法 JSON');
      return;
    }
    if (!tool) return;
    setRunning(true);
    setResult(null);
    try {
      const r = await testTool(tool.num, parsed);
      setResult(r);
    } catch (err) {
      notifyError(err, '试运行失败');
    } finally {
      setRunning(false);
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: '工具管理', to: listPath },
          { title: tool ? `试运行 · ${tool.name}` : '试运行' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            试运行 · {tool?.name ?? ''}
          </Title>
          {tool && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{tool.num}</Text> · 类型：
              <Tag color={tool.type === 'MCP' ? 'geekblue' : 'gold'} style={{ marginLeft: 4 }}>
                {tool.type} / {tool.subType}
              </Tag>
            </Paragraph>
          )}
        </div>
        <Space>
          <Button onClick={() => navigate(listPath)}>返回工具列表</Button>
          <Button type="primary" loading={running} onClick={handleRun} disabled={!tool}>
            执行
          </Button>
        </Space>
      </div>

      {loading || !tool ? (
        <Skeleton active paragraph={{ rows: 6 }} />
      ) : (
        <div style={{ maxWidth: 1000 }}>
          <Form form={form} layout="vertical">
            <Form.Item
              name="testInput"
              label="试运行入参（JSON，可留空）"
              extra={
                tool.type === 'MCP'
                  ? 'MCP 协议下入参可为空；FunctionCall 可填 path/query/header/body 等实参'
                  : 'FunctionCall 实参：path/query/header/body'
              }
            >
              <Input.TextArea rows={8} placeholder='{"path": "/", "query": {}}' />
            </Form.Item>
          </Form>

          {result && (
            <Card
              size="small"
              title={
                <Space>
                  <Text strong>结果</Text>
                  {result.success ? <Tag color="green">成功</Tag> : <Tag color="red">失败</Tag>}
                  {result.durationMs != null && (
                    <Text type="secondary">{result.durationMs} ms</Text>
                  )}
                </Space>
              }
              style={{ marginTop: 16 }}
            >
              {result.errorMessage && (
                <Alert
                  type="error"
                  showIcon
                  message={result.errorMessage}
                  style={{ marginBottom: 12 }}
                />
              )}
              {result.request && (
                <div style={{ marginBottom: 12 }}>
                  <Text strong>请求</Text>
                  <pre style={{ background: '#fafafa', padding: 8, borderRadius: 4, overflow: 'auto' }}>
                    {JSON.stringify(result.request, null, 2)}
                  </pre>
                </div>
              )}
              {result.response && (
                <div>
                  <Text strong>响应</Text>
                  <pre style={{ background: '#fafafa', padding: 8, borderRadius: 4, overflow: 'auto' }}>
                    {JSON.stringify(result.response, null, 2)}
                  </pre>
                </div>
              )}
            </Card>
          )}
        </div>
      )}
    </div>
  );
}
