import { ExclamationCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { ActionType, ModalForm, PageContainer, ProColumns, ProFormCheckbox, ProFormText, ProFormTextArea, ProTable } from '@ant-design/pro-components';
import { Button, Modal, Space, Tag, Typography, message } from 'antd';
import { useRef, useState } from 'react';
import type { UserRecord, UserRole, UserStatus } from '../../types/user';
import { createBusinessCode, initialUsers, roleOptions, statusColor, statusText } from '../../utils/user';

const { confirm } = Modal;

function statusGuard(record: UserRecord, expected: UserStatus, action: string) {
  if (record.status !== expected) {
    message.error(`仅${statusText[expected]}态用户允许${action}`);
    return false;
  }
  return true;
}

export default function UserManagementPage() {
  const actionRef = useRef<ActionType>();
  const [users, setUsers] = useState<UserRecord[]>(initialUsers);
  const [editingUser, setEditingUser] = useState<UserRecord>();
  const [formOpen, setFormOpen] = useState(false);
  const [resetUser, setResetUser] = useState<UserRecord>();

  const openCreate = () => {
    setEditingUser({
      id: String(Date.now()),
      businessCode: createBusinessCode(),
      email: '',
      phone: '',
      name: '',
      roles: ['普通用户'],
      status: 'draft',
      remark: '',
      updatedAt: new Date().toLocaleString('zh-CN', { hour12: false }),
    });
    setFormOpen(true);
  };

  const upsertUser = (values: Partial<UserRecord>) => {
    if (!editingUser) return;
    const next: UserRecord = {
      ...editingUser,
      ...values,
      roles: values.roles as UserRole[],
      updatedAt: new Date().toLocaleString('zh-CN', { hour12: false }),
    };
    setUsers((prev) => (prev.some((item) => item.id === next.id) ? prev.map((item) => (item.id === next.id ? next : item)) : [next, ...prev]));
    message.success('保存成功');
    setFormOpen(false);
    setEditingUser(undefined);
  };

  const submitUser = (record: UserRecord) => {
    if (!statusGuard(record, 'draft', '提交')) return;
    setUsers((prev) => prev.map((item) => (item.id === record.id ? { ...item, status: 'enabled', updatedAt: new Date().toLocaleString('zh-CN', { hour12: false }) } : item)));
    message.success('提交成功，用户已启用');
  };

  const deleteUser = (record: UserRecord) => {
    if (!statusGuard(record, 'draft', '删除')) return;
    confirm({
      title: '确认删除该草稿用户？',
      icon: <ExclamationCircleOutlined />,
      content: '删除后不可恢复。',
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: () => {
        setUsers((prev) => prev.filter((item) => item.id !== record.id));
        message.success('删除成功');
      },
    });
  };

  const changeStatus = (record: UserRecord, target: UserStatus) => {
    const expected = target === 'enabled' ? 'disabled' : 'enabled';
    const action = target === 'enabled' ? '启用' : '禁用';
    if (!statusGuard(record, expected, action)) return;
    confirm({
      title: `确认${action}该用户？`,
      icon: <ExclamationCircleOutlined />,
      okText: action,
      cancelText: '取消',
      onOk: () => {
        setUsers((prev) => prev.map((item) => (item.id === record.id ? { ...item, status: target, updatedAt: new Date().toLocaleString('zh-CN', { hour12: false }) } : item)));
        message.success(`${action}成功`);
      },
    });
  };

  const columns: ProColumns<UserRecord>[] = [
    {
      title: '业务编码',
      dataIndex: 'businessCode',
      width: 210,
      copyable: true,
      ellipsis: true,
    },
    {
      title: '姓名',
      dataIndex: 'name',
      width: 100,
    },
    {
      title: '角色',
      dataIndex: 'roles',
      valueType: 'select',
      fieldProps: {
        mode: 'multiple',
        options: roleOptions,
      },
      render: (_, record) => (
        <span className="role-tag-list">
          {record.roles.map((role) => (
            <Tag key={role} color={role === '管理员' ? 'blue' : 'green'}>
              {role}
            </Tag>
          ))}
        </span>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      ellipsis: true,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 130,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueEnum: {
        draft: { text: '草稿', status: 'Default' },
        enabled: { text: '启用', status: 'Success' },
        disabled: { text: '禁用', status: 'Error' },
      },
      render: (_, record) => <Tag color={statusColor[record.status]}>{statusText[record.status]}</Tag>,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      search: false,
      ellipsis: true,
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      search: false,
      width: 170,
    },
    {
      title: '操作',
      valueType: 'option',
      width: 220,
      render: (_, record) => {
        if (record.status === 'draft') {
          return [
            <a key="edit" onClick={() => { setEditingUser(record); setFormOpen(true); }}>编辑</a>,
            <a key="submit" onClick={() => submitUser(record)}>提交</a>,
            <Typography.Link key="delete" type="danger" onClick={() => deleteUser(record)}>删除</Typography.Link>,
          ];
        }
        if (record.status === 'enabled') {
          return [
            <a key="view" onClick={() => { setEditingUser(record); setFormOpen(true); }}>查看</a>,
            <a key="disable" onClick={() => changeStatus(record, 'disabled')}>禁用</a>,
            <a key="reset" onClick={() => setResetUser(record)}>重置密码</a>,
          ];
        }
        return [
          <a key="view" onClick={() => { setEditingUser(record); setFormOpen(true); }}>查看</a>,
          <a key="enable" onClick={() => changeStatus(record, 'enabled')}>启用</a>,
          <a key="reset" onClick={() => setResetUser(record)}>重置密码</a>,
        ];
      },
    },
  ];

  const readonly = editingUser?.status !== 'draft';

  return (
    <PageContainer title="用户管理" subTitle="平台级功能，仅管理员角色用户可访问">
      <ProTable<UserRecord>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        dataSource={users}
        search={{ labelWidth: 90 }}
        pagination={{ pageSize: 10 }}
        toolBarRender={() => [
          <Button key="new" type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新增
          </Button>,
        ]}
      />

      <ModalForm
        title={editingUser?.status === 'draft' ? '用户信息' : '查看用户'}
        open={formOpen}
        modalProps={{ destroyOnClose: true, onCancel: () => setFormOpen(false) }}
        initialValues={editingUser}
        submitter={readonly ? false : { searchConfig: { submitText: '保存', resetText: '取消' } }}
        onFinish={async (values) => {
          upsertUser(values as Partial<UserRecord>);
          return true;
        }}
      >
        <ProFormText name="businessCode" label="业务编码" readonly />
        <ProFormText name="email" label="邮箱" readonly={readonly} rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入正确的邮箱格式' }]} />
        <ProFormText name="phone" label="手机号" readonly={readonly} rules={[{ pattern: /^1\d{10}$/, message: '请输入正确的手机号格式' }]} />
        <ProFormText name="name" label="姓名" readonly={readonly} rules={[{ required: true, message: '请输入姓名' }]} />
        <ProFormCheckbox.Group name="roles" label="角色" disabled={readonly} options={roleOptions} rules={[{ required: true, message: '请至少选择一个角色' }]} />
        <ProFormText name="status" label="状态" readonly transform={() => ({})} fieldProps={{ value: editingUser ? statusText[editingUser.status] : '草稿' }} />
        <ProFormTextArea name="remark" label="备注" readonly={readonly} fieldProps={{ maxLength: 200, showCount: true }} rules={[{ max: 200, message: '备注不能超过 200 字' }]} />
      </ModalForm>

      <ModalForm
        title="重置密码"
        open={!!resetUser}
        modalProps={{ destroyOnClose: true, onCancel: () => setResetUser(undefined) }}
        submitter={{ searchConfig: { submitText: '确认重置', resetText: '取消' } }}
        onFinish={async (values) => {
          if (values.newPassword !== values.confirmPassword) {
            message.error('两次输入的密码不一致');
            return false;
          }
          message.success('密码已重置');
          setResetUser(undefined);
          return true;
        }}
      >
        <Space direction="vertical" style={{ marginBottom: 16 }}>
          <Typography.Text>用户：{resetUser?.name}</Typography.Text>
          <Typography.Text>编码：{resetUser?.businessCode}</Typography.Text>
          <Typography.Text>邮箱：{resetUser?.email}</Typography.Text>
        </Space>
        <ProFormText.Password name="newPassword" label="新密码" rules={[{ required: true, message: '请输入新密码' }, { min: 8, message: '密码至少 8 位' }]} />
        <ProFormText.Password name="confirmPassword" label="确认新密码" rules={[{ required: true, message: '请确认新密码' }]} />
      </ModalForm>
    </PageContainer>
  );
}
