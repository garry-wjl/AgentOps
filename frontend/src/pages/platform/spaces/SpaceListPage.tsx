import {
  CrownOutlined,
  DeleteOutlined,
  EditOutlined,
  EllipsisOutlined,
  PlusOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Dropdown,
  Empty,
  Input,
  Modal,
  Pagination,
  Space,
  Spin,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import type { MenuProps } from 'antd';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  deleteSpace,
  pageMine,
  type SpaceCardVO,
} from '@/api/space';
import { pageUsers } from '@/api/user';
import { useAuthStore } from '@/stores/authStore';
import { notifyError } from '@/utils/request';
import type { UserVO } from '@/types/api';

const { Title, Paragraph, Text } = Typography;

const PAGE_SIZE = 12;

/**
 * 空间卡片列表（平台 Shell 默认落地页）。
 *
 * 列表/删除在本页完成；新建/编辑跳转到独立全页：
 *   /platform/spaces/new
 *   /platform/spaces/:spaceCode/edit
 */
export default function SpaceListPage() {
  const navigate = useNavigate();
  const currentUserCode = useAuthStore((s) => s.currentUser?.num ?? '');

  const [spaces, setSpaces] = useState<SpaceCardVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNo, setPageNo] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [userOptions, setUserOptions] = useState<UserVO[]>([]);

  const [deleting, setDeleting] = useState<SpaceCardVO | null>(null);
  const [deleteSubmitting, setDeleteSubmitting] = useState(false);
  const [confirmText, setConfirmText] = useState('');

  const loadSpaces = useCallback(
    async (nextPage = pageNo, nextKeyword = keyword) => {
      setLoading(true);
      try {
        const result = await pageMine(nextKeyword || undefined, nextPage, PAGE_SIZE);
        setSpaces(result.records ?? []);
        setTotal(result.total ?? 0);
        setPageNo(result.pageNo ?? nextPage);
      } catch (err) {
        notifyError(err, '加载空间列表失败');
      } finally {
        setLoading(false);
      }
    },
    [pageNo, keyword],
  );

  useEffect(() => {
    pageUsers({ status: 'ENABLED', pageNo: 1, pageSize: 200 })
      .then((r) => setUserOptions(r.records ?? []))
      .catch((err) => notifyError(err, '加载用户列表失败'));
    loadSpaces(1, '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const expectedConfirm = deleting ? deleting.name : '';
  const canDelete = !!deleting && confirmText === expectedConfirm;

  const userOptionMap = useMemo(() => {
    const map = new Map<string, UserVO>();
    userOptions.forEach((u) => map.set(u.num, u));
    return map;
  }, [userOptions]);

  function renderOwner(card: SpaceCardVO): string {
    if (!card.ownerUserCode) return '—';
    const u = userOptionMap.get(card.ownerUserCode);
    return u?.name || card.ownerUserCode;
  }

  async function handleDelete() {
    if (!deleting) return;
    setDeleteSubmitting(true);
    try {
      await deleteSpace(deleting.num, confirmText);
      message.success(`空间 ${deleting.name} 已删除`);
      setDeleting(null);
      setConfirmText('');
      const newTotal = total - 1;
      const lastPage = Math.max(1, Math.ceil(newTotal / PAGE_SIZE));
      const targetPage = Math.min(pageNo, lastPage);
      await loadSpaces(targetPage, keyword);
    } catch (err) {
      notifyError(err, '删除空间失败');
    } finally {
      setDeleteSubmitting(false);
    }
  }

  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            空间管理
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            登录后默认落地页 · 卡片式浏览你参与的全部空间，点击「进入」切换至空间内
          </Paragraph>
        </div>
        <Space>
          <Input.Search
            placeholder="搜索空间名称或编码"
            allowClear
            onSearch={(v) => {
              setKeyword(v);
              loadSpaces(1, v);
            }}
            style={{ width: 260 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('new')}>
            新建空间
          </Button>
        </Space>
      </div>

      <Spin spinning={loading}>
        <div className="space-card-grid">
          <button type="button" className="space-card space-card-create" onClick={() => navigate('new')}>
            <PlusOutlined />
            <div>新建空间</div>
          </button>
          {spaces.length === 0 && !loading && (
            <Empty description="暂无空间" style={{ gridColumn: '1 / -1', marginTop: 24 }} />
          )}
          {spaces.map((s) => {
            const isAdmin = s.currentUserRole === 'ADMIN';
            const adminMenu: MenuProps['items'] = isAdmin
              ? [
                  { key: 'edit', icon: <EditOutlined />, label: '编辑' },
                  { key: 'delete', icon: <DeleteOutlined />, label: '删除', danger: true },
                ]
              : [];

            return (
              <Card
                key={s.num}
                className="space-card space-card-clickable"
                hoverable
                variant="outlined"
                onClick={() => navigate(`/spaces/${s.num}/agents`)}
              >
                <div className="space-card-head">
                  <span className="space-card-icon">
                    <CrownOutlined />
                  </span>
                  <div className="space-card-title">
                    <Text strong>{s.name}</Text>
                    <Tag color={isAdmin ? 'blue' : 'green'}>{isAdmin ? '管理' : '成员'}</Tag>
                  </div>
                  {adminMenu.length > 0 && (
                    <Dropdown
                      trigger={['hover', 'click']}
                      placement="bottomRight"
                      menu={{
                        items: adminMenu,
                        onClick: ({ key, domEvent }) => {
                          domEvent.stopPropagation();
                          if (key === 'edit') {
                            navigate(`/platform/spaces/${s.num}/edit`);
                          } else if (key === 'delete') {
                            setDeleting(s);
                            setConfirmText('');
                          }
                        },
                      }}
                    >
                      <button
                        type="button"
                        className="space-card-more"
                        onClick={(e) => e.stopPropagation()}
                        aria-label="更多操作"
                      >
                        <EllipsisOutlined />
                      </button>
                    </Dropdown>
                  )}
                </div>
                <Paragraph type="secondary" className="space-card-num">
                  {s.num}
                </Paragraph>
                <div className="space-card-meta">
                  <Tooltip title="所有者">
                    <Space size={4}>
                      <UserOutlined /> {renderOwner(s)}
                    </Space>
                  </Tooltip>
                  <Tooltip title="成员">
                    <Space size={4}>
                      <TeamOutlined /> {s.adminCount} 管理 / {s.memberCount} 成员
                    </Space>
                  </Tooltip>
                </div>
                <Paragraph ellipsis={{ rows: 1 }} className="space-card-remark" type="secondary">
                  {s.description || '—'}
                </Paragraph>
              </Card>
            );
          })}
        </div>
      </Spin>

      {total > PAGE_SIZE && (
        <div style={{ marginTop: 16, textAlign: 'right' }}>
          <Pagination
            current={pageNo}
            total={total}
            pageSize={PAGE_SIZE}
            showSizeChanger={false}
            onChange={(p) => loadSpaces(p, keyword)}
          />
        </div>
      )}

      <Modal
        title={
          <span>
            <span className="modal-warning-icon">⚠</span> 删除空间
          </span>
        }
        open={!!deleting}
        onCancel={() => {
          if (deleteSubmitting) return;
          setDeleting(null);
          setConfirmText('');
        }}
        okText="确定删除"
        cancelText="取消"
        confirmLoading={deleteSubmitting}
        okButtonProps={{ danger: true, disabled: !canDelete }}
        onOk={handleDelete}
      >
        {deleting && (
          <>
            <Alert
              type="warning"
              showIcon
              message="空间删除以后，空间里面的内容将不再可用。"
              style={{ marginBottom: 12 }}
            />
            <Paragraph>
              请输入空间名称 <Text code>{expectedConfirm}</Text> 以确认操作：
            </Paragraph>
            <Input
              value={confirmText}
              onChange={(e) => setConfirmText(e.target.value)}
              placeholder={expectedConfirm}
            />
          </>
        )}
      </Modal>
    </div>
  );
}
