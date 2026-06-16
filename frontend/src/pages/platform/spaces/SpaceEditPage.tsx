import { CrownOutlined, UserOutlined } from '@ant-design/icons';
import {
  Form,
  Input,
  Select,
  Skeleton,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  addMember,
  changeMemberRole,
  createSpace,
  getSpace,
  removeMember,
  updateSpaceBasic,
  type SpaceDTO,
} from '@/api/space';
import { pageUsers } from '@/api/user';
import { useAuthStore } from '@/stores/authStore';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';
import type { UserVO } from '@/types/api';

const { Title, Paragraph, Text } = Typography;

interface SpaceFormValues {
  name: string;
  remark?: string;
}

const PAGE_SIZE = 10;

/**
 * 空间新建/编辑 —— 整页表单。
 *
 * 路由：
 *   /platform/spaces/new
 *   /platform/spaces/:spaceCode/edit
 */
export default function SpaceEditPage() {
  const navigate = useNavigate();
  const { spaceCode } = useParams();
  const isEdit = !!spaceCode;
  const listPath = '/platform/spaces';
  const currentUserCode = useAuthStore((s) => s.currentUser?.num ?? '');

  const [editing, setEditing] = useState<SpaceDTO | null>(null);
  const [userOptions, setUserOptions] = useState<UserVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<SpaceFormValues>();

  const [adminUserCodes, setAdminUserCodes] = useState<string[]>([]);
  const [memberUserCodes, setMemberUserCodes] = useState<string[]>([]);
  const [originalAdmins, setOriginalAdmins] = useState<string[]>([]);
  const [originalMembers, setOriginalMembers] = useState<string[]>([]);

  /** 加载启用状态用户作为候选项 */
  useEffect(() => {
    pageUsers({ status: 'ENABLED', pageNo: 1, pageSize: 200 })
      .then((r) => setUserOptions(r.records ?? []))
      .catch((err) => notifyError(err, '加载用户列表失败'));
  }, []);

  useEffect(() => {
    if (!isEdit || !spaceCode) return;
    setLoading(true);
    getSpace(spaceCode)
      .then((detail) => {
        setEditing(detail);
        form.setFieldsValue({
          name: detail.name,
          remark: detail.description ?? '',
        });
        setAdminUserCodes(detail.adminUserCodes ?? []);
        setMemberUserCodes(detail.memberUserCodes ?? []);
        setOriginalAdmins(detail.adminUserCodes ?? []);
        setOriginalMembers(detail.memberUserCodes ?? []);
      })
      .catch((err) => {
        notifyError(err, '加载空间详情失败');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }, [isEdit, spaceCode, form, navigate, listPath]);

  const userOptionMap = useMemo(() => {
    const map = new Map<string, UserVO>();
    userOptions.forEach((u) => map.set(u.num, u));
    return map;
  }, [userOptions]);

  function renderUserLabel(code: string): string {
    if (code === currentUserCode) {
      const me = userOptionMap.get(code);
      const name = me?.name ?? currentUserCode;
      return `${name}（创建人，不可移除）`;
    }
    const u = userOptionMap.get(code);
    if (!u) return code;
    return `${u.name} · ${u.email}`;
  }

  /**
   * 计算成员 diff
   */
  function diffMembers(
    origAdmins: string[],
    origMembers: string[],
    nextAdmins: string[],
    nextMembers: string[],
  ) {
    const prevAdmin = new Set(origAdmins);
    const prevMember = new Set(origMembers);
    const nextAdmin = new Set(nextAdmins);
    const nextMember = new Set(nextMembers);
    const ownerCode = editing?.ownerUserCode;
    const toAddAdmin: string[] = [];
    const toAddMember: string[] = [];
    const toRemove: string[] = [];
    const toChangeAdmin: string[] = [];
    const toChangeMember: string[] = [];

    nextAdmin.forEach((u) => {
      if (prevAdmin.has(u)) return;
      if (prevMember.has(u)) toChangeAdmin.push(u);
      else toAddAdmin.push(u);
    });
    nextMember.forEach((u) => {
      if (prevMember.has(u)) return;
      if (prevAdmin.has(u)) toChangeMember.push(u);
      else toAddMember.push(u);
    });
    prevAdmin.forEach((u) => {
      if (!nextAdmin.has(u) && !nextMember.has(u) && u !== ownerCode) toRemove.push(u);
    });
    prevMember.forEach((u) => {
      if (!nextAdmin.has(u) && !nextMember.has(u)) toRemove.push(u);
    });
    return { toAddAdmin, toAddMember, toRemove, toChangeAdmin, toChangeMember };
  }

  async function handleSubmit() {
    let values: SpaceFormValues;
    try {
      values = await form.validateFields();
    } catch {
      return;
    }
    setSubmitting(true);
    try {
      if (isEdit && editing && spaceCode) {
        const ownerCode = editing.ownerUserCode;
        const finalAdmins = Array.from(new Set([ownerCode, ...adminUserCodes]));
        const finalMembers = memberUserCodes.filter((u) => u !== ownerCode && !finalAdmins.includes(u));
        if (values.name !== editing.name || (values.remark ?? '') !== (editing.description ?? '')) {
          await updateSpaceBasic(spaceCode, {
            name: values.name,
            description: values.remark ?? '',
          });
        }
        const diff = diffMembers(originalAdmins, originalMembers, finalAdmins, finalMembers);
        for (const u of diff.toAddAdmin) await addMember(spaceCode, u, 'ADMIN');
        for (const u of diff.toAddMember) await addMember(spaceCode, u, 'MEMBER');
        for (const u of diff.toChangeAdmin) await changeMemberRole(spaceCode, u, 'ADMIN');
        for (const u of diff.toChangeMember) await changeMemberRole(spaceCode, u, 'MEMBER');
        for (const u of diff.toRemove) await removeMember(spaceCode, u);
        message.success('已更新空间信息');
      } else {
        const created = await createSpace(values.name, values.remark || undefined);
        const ownerCode = created.ownerUserCode;
        const extraAdmins = adminUserCodes.filter((u) => u !== ownerCode);
        for (const u of extraAdmins) await addMember(created.num, u, 'ADMIN');
        for (const u of memberUserCodes) {
          if (u === ownerCode || extraAdmins.includes(u)) continue;
          await addMember(created.num, u, 'MEMBER');
        }
        message.success('空间创建成功');
      }
      navigate(listPath);
    } catch (err) {
      notifyError(err, isEdit ? '更新空间失败' : '创建空间失败');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: '空间管理', to: listPath },
          { title: isEdit ? `编辑 · ${editing?.name ?? ''}` : '新建空间' },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {isEdit ? `编辑空间 · ${editing?.name ?? ''}` : '新建空间'}
          </Title>
          {isEdit && editing && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              编码：<Text code>{editing.num}</Text>
            </Paragraph>
          )}
        </div>
        <Space>
          <button
            type="button"
            className="ant-btn ant-btn-default"
            onClick={() => navigate(listPath)}
            disabled={submitting}
          >
            <span>取消</span>
          </button>
          <button
            type="button"
            className="ant-btn ant-btn-primary"
            onClick={handleSubmit}
            disabled={submitting}
          >
            <span>{submitting ? '保存中…' : isEdit ? '保存' : '确定创建'}</span>
          </button>
        </Space>
      </div>

      {loading ? (
        <Skeleton active paragraph={{ rows: 8 }} />
      ) : (
        <Form form={form} layout="vertical" style={{ maxWidth: 900 }}>
          <Form.Item
            name="name"
            label="空间名称"
            rules={[{ required: true, message: '请输入空间名称' }, { max: 50 }]}
          >
            <Input maxLength={50} placeholder="1～50 字符" showCount />
          </Form.Item>
          <Form.Item label="管理员" required>
            <Select
              mode="multiple"
              value={adminUserCodes}
              onChange={(next: string[]) => {
                const ownerCode = editing?.ownerUserCode ?? currentUserCode;
                if (ownerCode && !next.includes(ownerCode)) {
                  next = [ownerCode, ...next];
                }
                setAdminUserCodes(next);
                setMemberUserCodes((prev) => prev.filter((u) => !next.includes(u)));
              }}
              optionLabelProp="label"
              placeholder="选择平台用户"
              style={{ width: '100%' }}
              options={userOptions.map((u) => {
                const ownerCode = editing?.ownerUserCode ?? currentUserCode;
                return {
                  value: u.num,
                  label:
                    u.num === ownerCode
                      ? `${u.name}（创建人，不可移除）`
                      : `${u.name} · ${u.email}`,
                  disabled: u.num === ownerCode,
                };
              })}
              tagRender={(props) => {
                const ownerCode = editing?.ownerUserCode ?? currentUserCode;
                const isOwner = props.value === ownerCode;
                return (
                  <Tag
                    color={isOwner ? 'gold' : 'blue'}
                    closable={!isOwner && props.closable}
                    onClose={props.onClose}
                    icon={isOwner ? <CrownOutlined /> : <UserOutlined />}
                    style={{ marginInlineEnd: 4 }}
                  >
                    {renderUserLabel(String(props.value))}
                  </Tag>
                );
              }}
            />
            <div className="form-hint">
              选项来自《用户管理》中状态为「启用」的用户；创建人始终为管理员且不可移除
            </div>
          </Form.Item>
          <Form.Item label="普通成员">
            <Select
              mode="multiple"
              value={memberUserCodes}
              onChange={(next: string[]) => {
                setMemberUserCodes(next.filter((u) => !adminUserCodes.includes(u)));
              }}
              placeholder="选择平台用户"
              style={{ width: '100%' }}
              options={userOptions.map((u) => ({
                value: u.num,
                label: `${u.name} · ${u.email}`,
                disabled: adminUserCodes.includes(u.num),
              }))}
            />
            <div className="form-hint">管理员名单中已选择的用户在普通成员中将被置灰</div>
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} maxLength={200} showCount />
          </Form.Item>
        </Form>
      )}
    </div>
  );
}
