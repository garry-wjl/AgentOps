import {
  Alert,
  Button,
  Form,
  Input,
  List,
  Modal,
  Select,
  Skeleton,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createAgent,
  deleteAgentVersion,
  deriveAgentVersion,
  editAgentAssembly,
  enableAgent,
  getAgent,
  listAgentVersions,
  prePublishCheck,
  publishAgentVersion,
  updateAgentBasic,
  type AgentDTO,
  type AgentVersionDTO,
  type AssemblySnapshotDTO,
} from '@/api/agent';
import { listEnabledModels, type ModelDTO } from '@/api/model';
import { listEnabledPrompts, type PromptDTO } from '@/api/prompt';
import { listEffectiveSkills, type SkillDTO } from '@/api/skill';
import { listEffectiveTools, type ToolDTO } from '@/api/tool';
import { listAvailableSandboxes, type SandboxDTO } from '@/api/sandbox';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

interface AgentBasicForm {
  name: string;
  displayName?: string;
  description?: string;
  tags?: string[];
  remark?: string;
  versionNo?: string;
}

/**
 * Agent 新建/编辑 —— 整页表单。
 *
 * 路由：
 *   /spaces/:spaceId/agents/new
 *   /spaces/:spaceId/agents/:agentNum/edit
 *
 * 新建：调 /agents/create，传 initialAssembly 创建首个 V1 草稿版本
 * 编辑：调 /agents/update-basic（仅基础字段）
 *   版本管理：列出版本列表，单独编辑草稿版本的 snapshot
 *   发布：先调 /agent-versions/pre-publish-check 通过后再 /agent-versions/publish
 */
export default function AgentEditPage() {
  const navigate = useNavigate();
  const { spaceId = '', agentNum } = useParams();
  const spaceCode = spaceId;
  const isEdit = !!agentNum;
  const listPath = `/spaces/${spaceId}/agents`;

  const [editing, setEditing] = useState<AgentDTO | null>(null);
  const [versions, setVersions] = useState<AgentVersionDTO[]>([]);
  const [models, setModels] = useState<ModelDTO[]>([]);
  const [prompts, setPrompts] = useState<PromptDTO[]>([]);
  const [skills, setSkills] = useState<SkillDTO[]>([]);
  const [tools, setTools] = useState<ToolDTO[]>([]);
  const [sandboxes, setSandboxes] = useState<SandboxDTO[]>([]);

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<AgentBasicForm>();

  /** Assembly 编辑 Modal 状态（仅编辑模式下打开 DRAFT 版本）。 */
  const [assemblyEditing, setAssemblyEditing] = useState<AgentVersionDTO | null>(null);
  const [assemblyForm] = Form.useForm<AssemblySnapshotDTO>();

  // ----- 加载详情与下拉选项 -----
  useEffect(() => {
    (async () => {
      try {
        const [m, p, s, t, sb] = await Promise.all([
          listEnabledModels(spaceCode),
          listEnabledPrompts(spaceCode),
          listEffectiveSkills(spaceCode),
          listEffectiveTools(spaceCode),
          listAvailableSandboxes(spaceCode),
        ]);
        setModels(m ?? []);
        setPrompts(p ?? []);
        setSkills(s ?? []);
        setTools(t ?? []);
        setSandboxes(sb ?? []);
      } catch (err) {
        notifyError(err, '加载下拉选项失败');
      }
    })();
  }, [spaceCode]);

  useEffect(() => {
    if (!isEdit || !agentNum) return;
    setLoading(true);
    (async () => {
      try {
        const detail = await getAgent(agentNum);
        setEditing(detail);
        form.setFieldsValue({
          name: detail.name,
          displayName: detail.displayName ?? '',
          description: detail.description ?? '',
          tags: detail.tags ?? [],
        });
        const vs = await listAgentVersions(detail.num);
        setVersions(vs);
      } catch (err) {
        notifyError(err, '加载 Agent 详情失败');
        navigate(listPath);
      } finally {
        setLoading(false);
      }
    })();
  }, [isEdit, agentNum, form, navigate, listPath]);

  // ----- 保存基础信息（编辑模式） -----
  async function handleSaveBasic() {
    let values: AgentBasicForm;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    if (!isEdit || !editing) return;
    setSubmitting(true);
    try {
      await updateAgentBasic(editing.num, {
        displayName: values.displayName,
        description: values.description,
        tags: values.tags,
        remark: values.remark,
      });
      message.success('已保存');
      navigate(listPath);
    } catch (err) {
      notifyError(err, '保存失败');
    } finally {
      setSubmitting(false);
    }
  }

  // ----- 创建新 Agent -----
  async function handleCreate(initialAssembly: AssemblySnapshotDTO | undefined) {
    let values: AgentBasicForm;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      const created = await createAgent(spaceCode, {
        name: values.name,
        displayName: values.displayName,
        description: values.description,
        tags: values.tags,
        remark: values.remark,
        versionNo: values.versionNo,
        initialAssembly,
      });
      message.success('Agent 已创建');
      navigate(`${listPath}/${created.num}/edit`);
    } catch (err) {
      notifyError(err, '创建失败');
    } finally {
      setSubmitting(false);
    }
  }

  // ----- 弹出 Assembly 编辑 Modal -----
  function openAssemblyEditor(v: AgentVersionDTO) {
    setAssemblyEditing(v);
    assemblyForm.setFieldsValue({
      modelCode: v.snapshot?.modelCode,
      modelParamsJson: v.snapshot?.modelParamsJson,
      systemPromptContent: v.snapshot?.systemPromptContent,
      systemPromptSourceCode: v.snapshot?.systemPromptSourceCode,
      userPromptContent: v.snapshot?.userPromptContent,
      userPromptSourceCode: v.snapshot?.userPromptSourceCode,
      skillCodes: v.snapshot?.skillCodes ?? [],
      toolCodes: v.snapshot?.toolCodes ?? [],
      sandboxCode: v.snapshot?.sandboxCode,
      shortMemoryTurns: v.snapshot?.shortMemoryTurns,
    });
  }

  async function handleSaveAssembly() {
    if (!assemblyEditing) return;
    let values: AssemblySnapshotDTO;
    try {
      values = await assemblyForm.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      const updated = await editAgentAssembly(assemblyEditing.num, values);
      message.success('草稿版本已更新');
      setVersions((prev) => prev.map((v) => (v.num === updated.num ? updated : v)));
      setAssemblyEditing(null);
    } catch (err) {
      notifyError(err, '保存草稿版本失败');
    } finally {
      setSubmitting(false);
    }
  }

  // ----- 发布前检查 + 发布 -----
  async function handlePublish(v: AgentVersionDTO) {
    try {
      const check = await prePublishCheck(v.num);
      if (!check.passed) {
        Modal.error({
          title: '发布前检查未通过',
          width: 600,
          content: (
            <div>
              {(check.errors ?? []).map((e) => (
                <div key={`${e.field}-${e.code}`} style={{ color: '#ff4d4f' }}>
                  • [{e.field}] {e.message}（{e.code}）
                </div>
              ))}
              {(check.warnings ?? []).map((w) => (
                <div key={`${w.field}-${w.code}`} style={{ color: '#faad14' }}>
                  ⚠ [{w.field}] {w.message}（{w.code}）
                </div>
              ))}
            </div>
          ),
        });
        return;
      }
      const updated = await publishAgentVersion(v.num);
      message.success('已发布');
      setVersions((prev) => prev.map((x) => (x.num === updated.num ? updated : x)));
      // 主体也要切到 EFFECTIVE
      if (editing) {
        try {
          await enableAgent(editing.num);
        } catch {
          // 主体状态切换失败不影响版本发布
        }
      }
    } catch (err) {
      notifyError(err, '发布失败');
    }
  }

  async function handleDeriveDraft(v: AgentVersionDTO) {
    const newVersionNo = window.prompt(
      `从 ${v.versionNo} 派生新草稿版本号（如 1.1.0）：`,
      '',
    );
    if (!newVersionNo?.trim()) return;
    try {
      const derived = await deriveAgentVersion(v.agentCode, v.num, newVersionNo.trim());
      message.success('已派生草稿版本');
      setVersions((prev) => [...prev, derived]);
    } catch (err) {
      notifyError(err, '派生失败');
    }
  }

  async function handleDeleteDraft(v: AgentVersionDTO) {
    try {
      await deleteAgentVersion(v.num);
      message.success('已删除草稿版本');
      setVersions((prev) => prev.filter((x) => x.num !== v.num));
    } catch (err) {
      notifyError(err, '删除失败');
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: 'Agent 管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '新建 Agent' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑 Agent · ${editing?.name ?? ''}` : '新建 Agent'}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text> · 状态：
              <Tag color={editing.status === 'EFFECTIVE' ? 'green' : 'default'} style={{ marginLeft: 4 }}>
                {editing.status}
              </Tag>
            </Paragraph>
          )}
        </div>
        <Space>
          <Button onClick={() => navigate(listPath)} disabled={submitting}>
            取消
          </Button>
          {isEdit ? (
            <Button type="primary" onClick={handleSaveBasic} loading={submitting}>
              保存
            </Button>
          ) : (
            <Button type="primary" onClick={() => handleCreate(undefined)} loading={submitting}>
              创建
            </Button>
          )}
        </Space>
      </div>

      {loading ? (
        <Skeleton active paragraph={{ rows: 8 }} />
      ) : (
        <>
          <Form form={form} layout="vertical" style={{ maxWidth: 900 }}>
            <Form.Item
              name="name"
              label="Agent 名称（英文，唯一）"
              rules={[{ required: true, message: '请输入名称' }, { max: 50 }]}
              extra={isEdit ? '名称不可修改' : undefined}
            >
              <Input maxLength={50} disabled={isEdit} placeholder="例如：customer_service" />
            </Form.Item>
            <Form.Item name="displayName" label="展示名">
              <Input maxLength={50} disabled={isEdit} placeholder="例如：客服总入口" />
            </Form.Item>
            <Form.Item name="description" label="描述">
              <Input.TextArea rows={2} maxLength={200} showCount />
            </Form.Item>
            <Form.Item name="tags" label="标签">
              <Select
                mode="tags"
                placeholder="按回车键确认标签"
                maxCount={10}
                tokenSeparators={[',', '，']}
              />
            </Form.Item>
            {!isEdit && (
              <Form.Item
                name="versionNo"
                label="初始版本号"
                extra="留空将使用 1.0.0；版本号需符合 semver 规范"
              >
                <Input placeholder="1.0.0" />
              </Form.Item>
            )}
          </Form>

          {/* 版本管理（仅编辑模式） */}
          {isEdit && editing && (
            <div style={{ maxWidth: 1100, marginTop: 24 }}>
              <Title level={5}>版本管理</Title>
              <List
                bordered
                dataSource={versions}
                locale={{ emptyText: '该 Agent 暂无版本' }}
                renderItem={(v) => {
                  const isDraft = v.status === 'DRAFT';
                  return (
                    <List.Item
                      actions={[
                        isDraft ? (
                          <a key="edit" onClick={() => openAssemblyEditor(v)}>
                            编辑 Assembly
                          </a>
                        ) : null,
                        isDraft ? (
                          <a key="publish" onClick={() => handlePublish(v)}>
                            发布
                          </a>
                        ) : null,
                        <a key="derive" onClick={() => handleDeriveDraft(v)}>
                          派生草稿
                        </a>,
                        isDraft ? (
                          <a
                            key="del"
                            style={{ color: '#ff4d4f' }}
                            onClick={() => handleDeleteDraft(v)}
                          >
                            删除草稿
                          </a>
                        ) : null,
                      ].filter(Boolean)}
                    >
                      <List.Item.Meta
                        title={
                          <Space>
                            <Text code>{v.versionNo}</Text>
                            <Tag
                              color={
                                v.status === 'ONLINE'
                                  ? 'green'
                                  : v.status === 'DRAFT'
                                  ? 'default'
                                  : 'red'
                              }
                            >
                              {v.status}
                            </Tag>
                            {v.snapshot?.modelCode && (
                              <Tag color="purple">模型 {v.snapshot.modelCode}</Tag>
                            )}
                          </Space>
                        }
                        description={
                          v.snapshot?.skillCodes?.length || v.snapshot?.toolCodes?.length ? (
                            <Space wrap size={4}>
                              {v.snapshot.skillCodes?.map((s) => (
                                <Tag key={s} color="cyan">
                                  {s}
                                </Tag>
                              ))}
                              {v.snapshot.toolCodes?.map((t) => (
                                <Tag key={t} color="gold">
                                  {t}
                                </Tag>
                              ))}
                              {v.snapshot.sandboxCode && (
                                <Tag color="blue">沙箱 {v.snapshot.sandboxCode}</Tag>
                              )}
                            </Space>
                          ) : (
                            <Text type="secondary">尚未配置 Assembly</Text>
                          )
                        }
                      />
                    </List.Item>
                  );
                }}
              />
            </div>
          )}

          {/* 新建模式下提示：可通过下方"先创建再编辑版本"或扩展"创建并初始化 Assembly" */}
          {!isEdit && (
            <Alert
              type="info"
              showIcon
              style={{ maxWidth: 900, marginTop: 16 }}
              message="新建 Agent 后会跳转到编辑页，可在那里为初始版本配置 Assembly（模型、Prompt、Skill、工具、沙箱）"
            />
          )}
        </>
      )}

      {/* Assembly 编辑 Modal */}
      <Modal
        title={assemblyEditing ? `编辑 Assembly · ${assemblyEditing.versionNo}` : ''}
        open={!!assemblyEditing}
        onCancel={() => setAssemblyEditing(null)}
        onOk={handleSaveAssembly}
        okText="保存"
        cancelText="取消"
        width={760}
        confirmLoading={submitting}
      >
        <Form form={assemblyForm} layout="vertical">
          <Form.Item name="modelCode" label="模型">
            <Select
              allowClear
              showSearch
              placeholder="选择空间内启用态模型"
              options={models.map((m) => ({
                value: m.num,
                label: `${m.name} · ${m.modelId}`,
              }))}
            />
          </Form.Item>
          <Form.Item name="modelParamsJson" label="模型参数（JSON，可选）">
            <Input.TextArea
              rows={3}
              placeholder='{"temperature": 0.7, "max_tokens": 2048}'
            />
          </Form.Item>
          <Form.Item
            name="systemPromptSourceCode"
            label="System Prompt（按编码引用）"
            extra="优先使用引用；当下面文本框填写时优先使用文本"
          >
            <Select
              allowClear
              showSearch
              placeholder="选择启用态 Prompt"
              options={prompts.map((p) => ({
                value: p.num,
                label: `${p.name} (${p.key})`,
              }))}
            />
          </Form.Item>
          <Form.Item name="systemPromptContent" label="System Prompt（直接文本，可选）">
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item
            name="userPromptSourceCode"
            label="User Prompt（按编码引用，可选）"
          >
            <Select
              allowClear
              showSearch
              placeholder="选择启用态 Prompt"
              options={prompts.map((p) => ({
                value: p.num,
                label: `${p.name} (${p.key})`,
              }))}
            />
          </Form.Item>
          <Form.Item name="userPromptContent" label="User Prompt（直接文本，可选）">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="skillCodes" label="绑定 Skill">
            <Select
              mode="multiple"
              allowClear
              placeholder="选择 Skill"
              options={skills.map((s) => ({ value: s.num, label: s.name }))}
            />
          </Form.Item>
          <Form.Item name="toolCodes" label="绑定工具">
            <Select
              mode="multiple"
              allowClear
              placeholder="选择工具"
              options={tools.map((t) => ({ value: t.num, label: `${t.name} · ${t.type}` }))}
            />
          </Form.Item>
          <Form.Item name="sandboxCode" label="代码沙箱（可选）">
            <Select
              allowClear
              showSearch
              placeholder="选择可用沙箱"
              options={sandboxes.map((s) => ({
                value: s.num,
                label: s.name,
              }))}
            />
          </Form.Item>
          <Form.Item name="shortMemoryTurns" label="短期记忆轮数">
            <Input type="number" min={0} max={50} placeholder="默认 0" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
