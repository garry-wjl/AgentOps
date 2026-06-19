import { CopyOutlined, EditOutlined } from '@ant-design/icons';
import {
  Alert,
  Button,
  Descriptions,
  Input,
  Modal,
  Popconfirm,
  Skeleton,
  Space,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  deleteSkillVersion,
  deriveSkillVersion,
  getSkill,
  listSkillVersions,
  publishSkillVersion,
  withdrawSkillVersion,
  type SkillDTO,
  type SkillVersionDTO,
} from '@/api/skill';
import { getSpace } from '@/api/space';
import { notifyError } from '@/utils/request';
import PageBreadcrumb from '@/components/PageBreadcrumb';
import { hasAdminRole, useAuthStore } from '@/stores/authStore';

const { Title, Paragraph, Text } = Typography;

const VERSION_STATUS: Record<
  SkillVersionDTO['status'],
  { color: string; label: string }
> = {
  DRAFT: { color: 'default', label: '草稿' },
  EFFECTIVE: { color: 'green', label: '生效' },
  WITHDRAWN: { color: 'orange', label: '下架' },
};

export default function SkillDetailPage() {
  const navigate = useNavigate();
  const { spaceId = '', skillId = '' } = useParams();
  const listPath = `/spaces/${spaceId}/skills`;

  const [detail, setDetail] = useState<SkillDTO | null>(null);
  const [versions, setVersions] = useState<SkillVersionDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState('');

  // 派生弹窗
  const [deriveOpen, setDeriveOpen] = useState(false);
  const [deriveSource, setDeriveSource] = useState<SkillVersionDTO | null>(null);
  const [deriveVersionNo, setDeriveVersionNo] = useState('');
  const [deriveSubmitting, setDeriveSubmitting] = useState(false);
  const [activeTab, setActiveTab] = useState('basic');

  const currentUser = useAuthStore((s) => s.currentUser);
  const [adminCodes, setAdminCodes] = useState<string[]>([]);
  const [ownerCode, setOwnerCode] = useState('');
  const isSpaceAdmin = adminCodes.includes(currentUser?.num ?? '') || ownerCode === currentUser?.num;
  const isAdmin = hasAdminRole(currentUser) || isSpaceAdmin;

  const activeVersionId = detail?.currentVersionNo
    ? versions.find((v) => v.versionNo === detail.currentVersionNo)?.num
    : undefined;

  const currentVersionStatus = detail?.currentVersionNo
    ? versions.find((v) => v.versionNo === detail.currentVersionNo)?.status
    : undefined;

  function loadDetail() {
    if (!skillId) return;
    setLoading(true);
    Promise.all([getSkill(skillId), listSkillVersions(skillId), getSpace(spaceId)])
      .then(([s, vers, sp]) => {
        setDetail(s);
        setVersions(vers);
        setAdminCodes(sp.adminUserCodes ?? []);
        setOwnerCode(sp.ownerUserCode ?? '');
      })
      .catch((err) => {
        notifyError(err, 'Skill 不存在或无权限访问');
        navigate(listPath);
      })
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [skillId]);

  function handleCopy(num: string) {
    navigator.clipboard.writeText(num).then(() => {
      message.success('已复制');
    });
  }

  // ----- 版本操作 -----

  async function handlePublish(version: SkillVersionDTO) {
    setActionLoading(version.num);
    try {
      await publishSkillVersion(version.num);
      message.success('发布成功');
      loadDetail();
    } catch (err) {
      notifyError(err, '发布失败');
    } finally {
      setActionLoading('');
    }
  }

  async function handleWithdraw(version: SkillVersionDTO) {
    setActionLoading(version.num);
    try {
      await withdrawSkillVersion(version.num);
      message.success('已下架');
      loadDetail();
    } catch (err) {
      notifyError(err, '下架失败');
    } finally {
      setActionLoading('');
    }
  }

  async function handleDelete(version: SkillVersionDTO) {
    setActionLoading(version.num);
    try {
      await deleteSkillVersion(version.num);
      message.success('已删除');
      loadDetail();
    } catch (err) {
      notifyError(err, '删除失败');
    } finally {
      setActionLoading('');
    }
  }

  function openDeriveModal(version: SkillVersionDTO) {
    setDeriveSource(version);
    const parts = version.versionNo.split('.');
    const patch = parseInt(parts[parts.length - 1] || '0', 10);
    const next = [...parts.slice(0, -1), String(patch + 1)].join('.');
    setDeriveVersionNo(next);
    setDeriveOpen(true);
  }

  async function handleDerive() {
    if (!detail || !deriveSource || !deriveVersionNo.trim()) return;
    setDeriveSubmitting(true);
    try {
      const result = await deriveSkillVersion(detail.num, deriveSource.num, deriveVersionNo.trim());
      message.success('新草稿已创建');
      setDeriveOpen(false);
      navigate(`${listPath}/${detail.num}/edit`);
    } catch (err) {
      notifyError(err, '派生失败');
    } finally {
      setDeriveSubmitting(false);
    }
  }

  const SEMVER_REGEX = /^\d+\.\d+\.\d+$/;
  const deriveValid = SEMVER_REGEX.test(deriveVersionNo.trim()) && deriveVersionNo.trim() !== deriveSource?.versionNo;

  // ----- 表格列 -----

  const columns: ColumnsType<SkillVersionDTO> = useMemo(
    () => [
      { title: '版本号', dataIndex: 'versionNo', width: 100 },
      { title: '版本编码', dataIndex: 'num', width: 200, render: (v: string) => <Text code>{v}</Text> },
      {
        title: '状态',
        dataIndex: 'status',
        width: 80,
        render: (s: SkillVersionDTO['status']) => (
          <Tag color={VERSION_STATUS[s].color}>{VERSION_STATUS[s].label}</Tag>
        ),
      },
      { title: '发布时间', dataIndex: 'publishTime', width: 160, render: (v?: string) => v ?? '—' },
      { title: '下架时间', dataIndex: 'withdrawTime', width: 160, render: (v?: string) => v ?? '—' },
      { title: '创建时间', dataIndex: 'createTime', width: 160 },
      {
        title: '操作',
        width: 260,
        render: (_: unknown, r: SkillVersionDTO) => {
          if (!isAdmin) return <Text type="secondary">—</Text>;
          if (r.status === 'DRAFT') {
            return (
              <Space size="small">
                <a onClick={() => navigate(`${listPath}/${skillId}/edit`)}>编辑</a>
                <Popconfirm
                  title={`即将发布版本 ${r.versionNo}`}
                  description={detail?.currentVersionNo ? `当前生效版本 ${detail.currentVersionNo} 将自动下架` : undefined}
                  onConfirm={() => handlePublish(r)}
                  okText="确定发布"
                  cancelText="取消"
                >
                  <a>发布</a>
                </Popconfirm>
                <Popconfirm
                  title="删除草稿版本"
                  description="删除后该版本的 SKILL.md 与资源文件将不可恢复"
                  onConfirm={() => handleDelete(r)}
                  okText="确定删除"
                  cancelText="取消"
                  okButtonProps={{ danger: true }}
                >
                  <a style={{ color: '#ff4d4f' }}>删除</a>
                </Popconfirm>
              </Space>
            );
          }
          if (r.status === 'EFFECTIVE') {
            return (
              <Space size="small">
                <a onClick={() => openDeriveModal(r)}>修改</a>
                <Popconfirm
                  title={`确定下架版本 ${r.versionNo}？`}
                  description="下架后，引用此 Skill 的 Agent 将无法加载该版本"
                  onConfirm={() => handleWithdraw(r)}
                  okText="确定下架"
                  cancelText="取消"
                >
                  <a>下架</a>
                </Popconfirm>
              </Space>
            );
          }
          return <a onClick={() => openDeriveModal(r)}>以此版本新建</a>;
        },
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [isAdmin, detail, listPath, skillId, deriveVersionNo, deriveSource],
  );

  // ----- 渲染 -----

  if (loading || !detail) {
    return (
      <div className="page-section">
        <Skeleton active paragraph={{ rows: 10 }} />
      </div>
    );
  }

  return (
    <div className="page-section">
      <PageBreadcrumb
        items={[
          { title: 'Skill 管理', to: listPath },
          { title: detail.name },
        ]}
      />
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {detail.name}
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            编号：<Text code>{detail.num}</Text>
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => handleCopy(detail.num)}
              style={{ marginLeft: 4 }}
            />
          </Paragraph>
          <Paragraph type="secondary" style={{ margin: '4px 0 0' }}>
            当前版本：{detail.currentVersionNo ?? '—'}
            {currentVersionStatus && (
              <Tag color={VERSION_STATUS[currentVersionStatus].color} style={{ marginLeft: 8 }}>
                {VERSION_STATUS[currentVersionStatus].label}
              </Tag>
            )}
          </Paragraph>
        </div>
        <Space>
          {isAdmin && (
            <Button
              type="primary"
              icon={<EditOutlined />}
              onClick={() => navigate(`${listPath}/${skillId}/edit`)}
            >
              编辑基本信息
            </Button>
          )}
        </Space>
      </div>

      <div style={{ marginTop: 16 }}>
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          {
            key: 'basic',
            label: '基本信息',
            children: (
              <Descriptions column={2} bordered size="middle" style={{ maxWidth: 960 }}>
                <Descriptions.Item label="名称" span={2}>
                  <Text strong>{detail.name}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="描述" span={2}>
                  {detail.description ? <Text>{detail.description}</Text> : <Text type="secondary">—</Text>}
                </Descriptions.Item>
                <Descriptions.Item label="业务编码" span={2}>
                  <Text code>{detail.num}</Text>
                  <Button
                    type="text"
                    size="small"
                    icon={<CopyOutlined />}
                    onClick={() => handleCopy(detail.num)}
                    style={{ marginLeft: 4 }}
                  />
                </Descriptions.Item>
                <Descriptions.Item label="当前版本">
                  {detail.currentVersionNo ? <Text code>{detail.currentVersionNo}</Text> : <Text type="secondary">—</Text>}
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  <Tag color={detail.status === 'EFFECTIVE' ? 'green' : detail.status === 'DRAFT' ? 'default' : 'red'}>
                    {detail.status === 'EFFECTIVE' ? '启用' : detail.status === 'DRAFT' ? '草稿' : '停用'}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="标签" span={2}>
                  <Space wrap size={4}>
                    {(!detail.tags || detail.tags.length === 0) && <Text type="secondary">—</Text>}
                    {detail.tags?.map((t) => (
                      <Tag key={t}>{t}</Tag>
                    ))}
                  </Space>
                </Descriptions.Item>
                <Descriptions.Item label="备注" span={2}>
                  {detail.remark ? <Text>{detail.remark}</Text> : <Text type="secondary">—</Text>}
                </Descriptions.Item>
                <Descriptions.Item label="创建时间">{detail.createTime || '—'}</Descriptions.Item>
                <Descriptions.Item label="更新时间">{detail.updateTime || '—'}</Descriptions.Item>
              </Descriptions>
            ),
          },
          {
            key: 'versions',
            label: '版本管理',
            children: (
              <Table<SkillVersionDTO>
                rowKey="num"
                dataSource={versions}
                columns={columns}
                pagination={false}
                scroll={{ x: 1000 }}
                size="middle"
                locale={{ emptyText: '暂无版本记录' }}
                rowClassName={(r) => (r.num === activeVersionId ? 'version-row-active' : '')}
              />
            ),
          },
        ]}
        />
      </div>

      <style>{`
        .version-row-active {
          background-color: #f6ffed !important;
        }
        .version-row-active td:first-child {
          border-left: 3px solid #52c41a;
        }
      `}</style>
      <Modal
        title={deriveSource ? `基于 ${deriveSource.versionNo} 创建新版本` : '派生新版本'}
        open={deriveOpen}
        onCancel={() => {
          setDeriveOpen(false);
          setDeriveSource(null);
          setDeriveVersionNo('');
        }}
        onOk={handleDerive}
        okText="创建并进入编辑"
        cancelText="取消"
        confirmLoading={deriveSubmitting}
        okButtonProps={{ disabled: !deriveValid }}
      >
        <div style={{ marginBottom: 16 }}>
          <div style={{ marginBottom: 8 }}>
            新版本号 <span style={{ color: '#ff4d4f' }}>*</span>
          </div>
          <Input
            value={deriveVersionNo}
            onChange={(e) => setDeriveVersionNo(e.target.value)}
            placeholder="例如：1.3.0"
          />
          <div style={{ marginTop: 4, fontSize: 12, color: '#8c8c8c' }}>
            不可与该 Skill 已有版本重复；建议使用 SemVer 格式（x.y.z）
          </div>
          {!SEMVER_REGEX.test(deriveVersionNo.trim()) && deriveVersionNo.trim() && (
            <Alert type="warning" showIcon message="版本号格式无效，请使用 SemVer 格式（如 1.0.0）" style={{ marginTop: 8 }} />
          )}
        </div>
        {deriveSource && (
          <Alert
            type="info"
            showIcon
            message={`系统将复制 ${deriveSource.versionNo} 的 SKILL.md 与资源文件作为新草稿。`}
          />
        )}
      </Modal>

      <style>{`
        .version-row-active {
          background-color: #f6ffed !important;
        }
        .version-row-active td:first-child {
          border-left: 3px solid #52c41a;
        }
      `}</style>
    </div>
  );
}