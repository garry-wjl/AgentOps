import {
  DeleteOutlined,
  EditOutlined,
  FileAddOutlined,
  FileOutlined,
  FolderOpenOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Empty,
  Form,
  Input,
  List,
  Modal,
  Popconfirm,
  Select,
  Skeleton,
  Space,
  Tabs,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  createResourceFile,
  createSkill,
  deleteResource,
  getSkill,
  listResourceFiles,
  listSkillVersions,
  renameResource,
  updateResourceContent,
  updateSkillBasic,
  type ResourceFileType,
  type SkillDTO,
  type SkillResourceFileDTO,
  type SkillVersionDTO,
} from '@/api/skill';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';

const { Title, Paragraph, Text } = Typography;

/**
 * 暂存于前端、待保存时随 Skill 一并创建的资源条目。
 */
interface PendingResource {
  tempId: string;
  path: string;
  type: ResourceFileType;
  content: string;
}

interface SkillFormValues {
  name: string;
  description?: string;
  tags?: string[];
  remark?: string;
  initialSkillMd?: string;
}

/**
 * Skill 新建/编辑 —— 单表单 + 「SKILL.md」内嵌 Tab。
 *
 * 排版：
 *   基础信息（name / description / tags / remark）
 *   → SKILL.md（内嵌 Tabs：内容 / 资源文件）
 *
 * 路由：
 *   /spaces/:spaceId/skills/new
 *   /spaces/:spaceId/skills/:skillId/edit
 */
export default function SkillEditPage() {
  const navigate = useNavigate();
  const { spaceId = '', skillId } = useParams();
  const spaceCode = spaceId;
  const isEdit = !!skillId;
  const listPath = `/spaces/${spaceId}/skills`;

  const [editing, setEditing] = useState<SkillDTO | null>(null);
  const [versions, setVersions] = useState<SkillVersionDTO[]>([]);
  const [remoteResources, setRemoteResources] = useState<SkillResourceFileDTO[]>([]);
  const [pendingResources, setPendingResources] = useState<PendingResource[]>([]);

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<SkillFormValues>();

  // 资源编辑 Modal
  const [resourceModalOpen, setResourceModalOpen] = useState(false);
  const [resourceEditing, setResourceEditing] = useState<PendingResource | null>(null);
  const [resourceForm] = Form.useForm<{ path: string; type: ResourceFileType; content: string }>();

  // 远端资源行内编辑
  const [editingContent, setEditingContent] = useState<{ num: string; value: string } | null>(null);

  // ----- 加载详情（编辑模式） -----
  useEffect(() => {
    if (!isEdit || !skillId) return;
    setLoading(true);
    (async () => {
      try {
        const detail = await getSkill(skillId);
        setEditing(detail);
        form.setFieldsValue({
          name: detail.name,
          description: detail.description ?? '',
          tags: detail.tags ?? [],
          remark: detail.remark ?? '',
        });
        const vers = await listSkillVersions(detail.num);
        setVersions(vers);
        const draft = vers.find((v) => v.status === 'DRAFT');
        if (draft) {
          const files = await listResourceFiles(draft.num);
          setRemoteResources(files);
          form.setFieldValue('initialSkillMd', draft.skillMdContent ?? '');
        } else if (vers.length > 0) {
          form.setFieldValue('initialSkillMd', vers[0].skillMdContent ?? '');
        }
      } catch (err) {
        notifyError(err, '加载 Skill 详情失败');
        navigate(listPath);
      } finally {
        setLoading(false);
      }
    })();
  }, [isEdit, skillId, form, navigate, listPath]);

  const draftVersion = useMemo(() => versions.find((v) => v.status === 'DRAFT'), [versions]);

  // ----- 表单字段 ↔ SKILL.md 双向同步 -----
  const watchedName = Form.useWatch('name', form);
  const watchedDescription = Form.useWatch('description', form);
  const watchedSkillMd = Form.useWatch('initialSkillMd', form);
  /** 防止「解析回写」与「字段回写」互踩的标记。 */
  const syncingRef = useRef(false);

  // 字段 → SKILL.md：name/description 变化时回写 frontmatter
  useEffect(() => {
    if (syncingRef.current) {
      syncingRef.current = false;
      return;
    }
    if (watchedName == null && watchedDescription == null) return;
    const current = typeof watchedSkillMd === 'string' ? watchedSkillMd : '';
    const parsed = parseFrontmatter(current);
    const nextName = watchedName ?? parsed.name ?? '';
    const nextDesc = watchedDescription ?? parsed.description ?? '';
    if (parsed.name === nextName && parsed.description === nextDesc) return;
    syncingRef.current = true;
    form.setFieldValue('initialSkillMd', buildSkillMd(current, nextName, nextDesc));
  }, [watchedName, watchedDescription]); // eslint-disable-line react-hooks/exhaustive-deps

  /** SKILL.md → 字段：textarea 变更时解析 frontmatter 回填 name/description。 */
  function handleSkillMdChange(e: React.ChangeEvent<HTMLTextAreaElement>) {
    const next = e.target.value;
    form.setFieldValue('initialSkillMd', next);
    const parsed = parseFrontmatter(next);
    if (parsed.name != null && parsed.name !== form.getFieldValue('name')) {
      syncingRef.current = true;
      form.setFieldValue('name', parsed.name);
    }
    if (parsed.description != null && parsed.description !== form.getFieldValue('description')) {
      syncingRef.current = true;
      form.setFieldValue('description', parsed.description);
    }
  }

  // ----- 保存 -----
  async function handleSave() {
    let values: SkillFormValues;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      if (isEdit && editing) {
        await updateSkillBasic(editing.num, {
          description: values.description,
          tags: values.tags,
          remark: values.remark,
        });
        message.success('已保存');
        navigate(listPath);
      } else {
        const created = await createSkill(spaceCode, {
          name: values.name,
          description: values.description,
          tags: values.tags,
          remark: values.remark,
          initialSkillMd: values.initialSkillMd,
        });
        const vers = await listSkillVersions(created.num);
        const draft = vers.find((v) => v.status === 'DRAFT');
        if (draft) {
          for (const r of pendingResources) {
            await createResourceFile(draft.num, { path: r.path, type: r.type, content: r.content });
          }
        }
        message.success('Skill 已创建');
        navigate(listPath);
      }
    } catch (err) {
      notifyError(err, isEdit ? '保存失败' : '创建失败');
    } finally {
      setSubmitting(false);
    }
  }

  // ----- 资源 Modal 提交（仅新建模式） -----
  function handleResourceModalSubmit() {
    resourceForm
      .validateFields()
      .then((values) => {
        if (resourceEditing) {
          setPendingResources((prev) =>
            prev.map((r) => (r.tempId === resourceEditing.tempId ? { ...r, ...values } : r)),
          );
        } else {
          setPendingResources((prev) => [
            ...prev,
            { tempId: `tmp-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`, ...values },
          ]);
        }
        setResourceModalOpen(false);
        setResourceEditing(null);
        resourceForm.resetFields();
      })
      .catch(() => undefined);
  }

  function openAddResource() {
    setResourceEditing(null);
    resourceForm.resetFields();
    resourceForm.setFieldsValue({ type: 'FILE' });
    setResourceModalOpen(true);
  }

  function openEditPending(r: PendingResource) {
    setResourceEditing(r);
    resourceForm.setFieldsValue({ path: r.path, type: r.type, content: r.content });
    setResourceModalOpen(true);
  }

  function removePending(tempId: string) {
    setPendingResources((prev) => prev.filter((r) => r.tempId !== tempId));
  }

  // ----- 编辑模式：远端资源操作 -----
  async function saveRemoteContent(num: string) {
    if (!editingContent) return;
    try {
      await updateResourceContent(num, editingContent.value);
      message.success('资源已更新');
      setEditingContent(null);
      const draft = versions.find((v) => v.status === 'DRAFT');
      if (draft) {
        const files = await listResourceFiles(draft.num);
        setRemoteResources(files);
      }
    } catch (err) {
      notifyError(err, '资源更新失败');
    }
  }

  function handleRenameRemote(r: SkillResourceFileDTO) {
    let next = '';
    Modal.confirm({
      title: '重命名资源',
      content: <Input id="rename-input" defaultValue={r.path} onChange={(e) => (next = e.target.value)} />,
      onOk: async () => {
        if (!next.trim() || next.trim() === r.path) return;
        try {
          await renameResource(r.num, next.trim());
          message.success('已重命名');
          const draft = versions.find((v) => v.status === 'DRAFT');
          if (draft) {
            const files = await listResourceFiles(draft.num);
            setRemoteResources(files);
          }
        } catch (err) {
          notifyError(err, '重命名失败');
        }
      },
    });
  }

  async function handleDeleteRemote(r: SkillResourceFileDTO) {
    try {
      await deleteResource(r.num);
      message.success('已删除');
      const draft = versions.find((v) => v.status === 'DRAFT');
      if (draft) {
        const files = await listResourceFiles(draft.num);
        setRemoteResources(files);
      }
    } catch (err) {
      notifyError(err, '删除失败');
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: 'Skill 管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '新建 Skill' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑 Skill · ${editing?.name ?? ''}` : '新建 Skill'}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text>
            </Paragraph>
          )}
        </div>
        <Space>
          <Button onClick={() => navigate(listPath)} disabled={submitting}>
            取消
          </Button>
          <Button type="primary" onClick={handleSave} loading={submitting}>
            保存
          </Button>
        </Space>
      </div>

      {loading ? (
        <Skeleton active paragraph={{ rows: 10 }} />
      ) : (
        <Form form={form} layout="vertical" style={{ maxWidth: 1100 }}>
          {/* 基础信息 */}
          <Form.Item
            name="name"
            label="名称"
            rules={[{ required: true, message: '请输入名称' }, { max: 50 }]}
          >
            <Input maxLength={50} disabled={isEdit} placeholder="例如：客服知识检索" />
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
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} showCount />
          </Form.Item>

          {/* SKILL.md 与资源文件 —— 内嵌 Tab */}
          <Form.Item
            label="内容"
            extra={
              isEdit && !draftVersion
                ? '当前无 DRAFT 状态版本，SKILL.md 与资源文件均不可编辑'
                : undefined
            }
          >
            <Tabs
              defaultActiveKey="content"
              items={[
                {
                  key: 'content',
                  label: (
                    <span>
                      <FileOutlined /> Skill.md
                    </span>
                  ),
                  children: (
                    <Form.Item
                      name="initialSkillMd"
                      noStyle
                      rules={[{ required: !isEdit, message: '请输入 SKILL.md 内容' }]}
                    >
                      <Input.TextArea
                        rows={18}
                        disabled={isEdit && !draftVersion}
                        onChange={handleSkillMdChange}
                        placeholder={'---\nname: ...\ndescription: ...\nversion: 1.0.0\n---\n# 标题\nMarkdown 内容'}
                      />
                    </Form.Item>
                  ),
                },
                {
                  key: 'resources',
                  label: (
                    <span>
                      <FolderOpenOutlined /> 资源文件
                      {!isEdit && pendingResources.length > 0 && (
                        <Tag color="blue" style={{ marginLeft: 6 }}>
                          {pendingResources.length}
                        </Tag>
                      )}
                      {isEdit && remoteResources.length > 0 && (
                        <Tag color="blue" style={{ marginLeft: 6 }}>
                          {remoteResources.length}
                        </Tag>
                      )}
                    </span>
                  ),
                  children: (
                    <>
                      {!isEdit && (
                        <Alert
                          type="info"
                          showIcon
                          message="资源文件会在 Skill 保存时随 V1 草稿版本一并创建。"
                          style={{ marginBottom: 12 }}
                        />
                      )}
                      {isEdit && !draftVersion && (
                        <Alert
                          type="warning"
                          showIcon
                          message="当前无 DRAFT 状态版本，无法新增或修改资源。"
                          style={{ marginBottom: 12 }}
                        />
                      )}
                      <div style={{ marginBottom: 12 }}>
                        <Button
                          type="dashed"
                          icon={<FileAddOutlined />}
                          onClick={openAddResource}
                          disabled={isEdit && !draftVersion}
                        >
                          新增资源
                        </Button>
                      </div>

                      {/* 新建模式：暂存列表 */}
                      {!isEdit && (
                        <List
                          bordered
                          dataSource={pendingResources}
                          locale={{ emptyText: <Empty description="尚未新增资源" /> }}
                          renderItem={(r) => (
                            <List.Item
                              actions={[
                                <a key="edit" onClick={() => openEditPending(r)}>
                                  编辑
                                </a>,
                                <Popconfirm
                                  key="del"
                                  title="移除该资源？"
                                  onConfirm={() => removePending(r.tempId)}
                                >
                                  <a style={{ color: '#ff4d4f' }}>
                                    <DeleteOutlined /> 移除
                                  </a>
                                </Popconfirm>,
                              ]}
                            >
                              <List.Item.Meta
                                avatar={r.type === 'FOLDER' ? <FolderOpenOutlined /> : <FileOutlined />}
                                title={
                                  <Space>
                                    <Text code>{r.path}</Text>
                                    <Tag color={r.type === 'FOLDER' ? 'gold' : 'blue'}>
                                      {r.type === 'FOLDER' ? '目录' : '文件'}
                                    </Tag>
                                  </Space>
                                }
                                description={
                                  r.type === 'FILE' ? (
                                    <Text type="secondary" style={{ fontSize: 12 }}>
                                      {r.content ? `${r.content.length} 字符` : '空内容'}
                                    </Text>
                                  ) : (
                                    <Text type="secondary" style={{ fontSize: 12 }}>
                                      —
                                    </Text>
                                  )
                                }
                              />
                            </List.Item>
                          )}
                        />
                      )}

                      {/* 编辑模式：远端资源列表 */}
                      {isEdit && (
                        <List
                          bordered
                          dataSource={remoteResources}
                          locale={{ emptyText: <Empty description="该版本暂无资源" /> }}
                          renderItem={(r) => {
                            const isEditing = editingContent?.num === r.num;
                            return (
                              <List.Item
                                actions={[
                                  <a
                                    key="toggle"
                                    onClick={() =>
                                      isEditing
                                        ? setEditingContent(null)
                                        : setEditingContent({ num: r.num, value: r.content ?? '' })
                                    }
                                  >
                                    {isEditing ? '取消' : '编辑内容'}
                                  </a>,
                                  isEditing ? (
                                    <a key="save" onClick={() => saveRemoteContent(r.num)}>
                                      保存
                                    </a>
                                  ) : null,
                                  <a key="rename" onClick={() => handleRenameRemote(r)}>
                                    <EditOutlined /> 重命名
                                  </a>,
                                  <Popconfirm
                                    key="del"
                                    title={`确认删除资源「${r.path}」？`}
                                    onConfirm={() => handleDeleteRemote(r)}
                                  >
                                    <a style={{ color: '#ff4d4f' }}>
                                      <DeleteOutlined /> 删除
                                    </a>
                                  </Popconfirm>,
                                ].filter(Boolean)}
                              >
                                <List.Item.Meta
                                  avatar={r.type === 'FOLDER' ? <FolderOpenOutlined /> : <FileOutlined />}
                                  title={
                                    <Space>
                                      <Text code>{r.path}</Text>
                                      <Tag color={r.type === 'FOLDER' ? 'gold' : 'blue'}>
                                        {r.type === 'FOLDER' ? '目录' : '文件'}
                                      </Tag>
                                    </Space>
                                  }
                                  description={
                                    r.type === 'FOLDER' ? (
                                      <Text type="secondary" style={{ fontSize: 12 }}>
                                        —
                                      </Text>
                                    ) : isEditing ? (
                                      <Input.TextArea
                                        autoSize={{ minRows: 3, maxRows: 10 }}
                                        value={editingContent?.value}
                                        onChange={(e) =>
                                          setEditingContent({ num: r.num, value: e.target.value })
                                        }
                                      />
                                    ) : (
                                      <Tooltip title={r.content}>
                                        <Text type="secondary" style={{ fontSize: 12 }}>
                                          {r.content ? `${r.content.length} 字符` : '空内容'}
                                        </Text>
                                      </Tooltip>
                                    )
                                  }
                                />
                              </List.Item>
                            );
                          }}
                        />
                      )}
                    </>
                  ),
                },
              ]}
            />
          </Form.Item>
        </Form>
      )}

      {/* 资源编辑/新增 Modal（仅新建模式使用） */}
      <Modal
        title={resourceEditing ? '编辑暂存资源' : '新增资源'}
        open={resourceModalOpen}
        onCancel={() => {
          setResourceModalOpen(false);
          setResourceEditing(null);
          resourceForm.resetFields();
        }}
        onOk={handleResourceModalSubmit}
        okText="确定"
        cancelText="取消"
        destroyOnClose
      >
        <Form form={resourceForm} layout="vertical">
          <Form.Item
            name="path"
            label="路径"
            rules={[
              { required: true, message: '请输入路径' },
              { pattern: /^[A-Za-z0-9._\-\/]{1,256}$/, message: '英文字母/数字/._-/，1～256 字符' },
            ]}
          >
            <Input placeholder="例如：resources/data.json 或 docs/index.md" />
          </Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true }]}>
            <Select
              options={[
                { value: 'FILE', label: '文件' },
                { value: 'FOLDER', label: '目录' },
              ]}
            />
          </Form.Item>
          <Form.Item shouldUpdate={(prev, next) => prev.type !== next.type} noStyle>
            {() =>
              resourceForm.getFieldValue('type') === 'FOLDER' ? null : (
                <Form.Item name="content" label="内容">
                  <Input.TextArea rows={8} placeholder="文件内容" />
                </Form.Item>
              )
            }
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

/**
 * 从 SKILL.md 中解析 YAML frontmatter。
 * 期望格式（与后端默认模板一致）：
 *   ---
 *   name: ...
 *   description: ...
 *   version: 1.0.0
 *   ---
 *   <body>
 *
 * 仅解析顶部的 `name` / `description` 字段，其它原样保留。
 * 无 frontmatter 时返回空对象，body 为原文。
 */
function parseFrontmatter(text: string): { name?: string; description?: string; body: string } {
  if (!text) return { body: '' };
  const match = text.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/);
  if (!match) return { body: text };
  const header = match[1];
  const body = match[2];
  const result: { name?: string; description?: string; body: string } = { body };
  for (const line of header.split(/\r?\n/)) {
    const m = line.match(/^(name|description)\s*:\s*(.*)$/);
    if (m) {
      const key = m[1] as 'name' | 'description';
      result[key] = m[2].trim();
    }
  }
  return result;
}

/**
 * 用给定的 name / description 重建 SKILL.md。
 * 保留原文中的其它 frontmatter 字段（如 version）以及正文。
 * 若原文本没有 frontmatter，则自动补一个标准模板。
 */
function buildSkillMd(original: string, name: string, description: string): string {
  const fmMatch = original.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/);
  if (!fmMatch) {
    // 无 frontmatter：补一个标准 frontmatter，正文保持原样
    return `---\nname: ${name}\ndescription: ${description}\nversion: 1.0.0\n---\n${original}`;
  }
  const headerLines = fmMatch[1].split(/\r?\n/);
  const body = fmMatch[2];
  const updated: string[] = [];
  let nameSet = false;
  let descSet = false;
  for (const line of headerLines) {
    if (/^name\s*:/i.test(line)) {
      updated.push(`name: ${name}`);
      nameSet = true;
    } else if (/^description\s*:/i.test(line)) {
      updated.push(`description: ${description}`);
      descSet = true;
    } else {
      updated.push(line);
    }
  }
  if (!nameSet) updated.unshift(`name: ${name}`);
  if (!descSet) updated.splice(1, 0, `description: ${description}`);
  return `---\n${updated.join('\n')}\n---\n${body}`;
}
