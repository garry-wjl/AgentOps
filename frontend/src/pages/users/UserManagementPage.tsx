import { ExclamationCircleOutlined, PlusOutlined } from '@ant-design/icons';
import {
  ActionType,
  ModalForm,
  PageContainer,
  ProColumns,
  ProFormCheckbox,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { Button, Modal, Space, Tag, Typography, message } from 'antd';
import { useEffect, useRef, useState } from 'react';
import {
  createUser,
  deleteUser,
  detailUser,
  disableUser,
  enableUser,
  pageUsers,
  resetPassword,
  roleOptions as roleOptionsApi,
  saveUser,
  submitUser,
} from '@/api/user';
import type { UserRoleDTO, UserStatus, UserVO } from '@/types/api';

const { confirm } = Modal;

const STATUS_TEXT: Record<UserStatus, string> = {
  DRAFT: '草稿',
  ENABLED: '启用',
  DISABLED: '禁用',
};

const STATUS_COLOR: Record<UserStatus, string> = {
  DRAFT: 'default',
  ENABLED: 'success',
  DISABLED: 'error',
};

interface FormState {
  mode: 'create' | 'edit' | 'view';
  user?: UserVO;
}

export default function UserManagementPage() {
  const actionRef = useRef<ActionType>();
  const [roles, setRoles] = useState<UserRoleDTO[]>([]);
  const [formState, setFormState] = useState<FormState | null>(null);
  const [resetUser, setResetUser] = useState<UserVO | null>(null);

  useEffect(() => {
    roleOptionsApi()
      .then(setRoles)
      .catch(() => setRoles([
        { code: 'ADMIN', label: '管理员' },
        { code: 'MEMBER', label: '普通成员' },
      ]));
  }, []);

  const roleSelectOptions = roles.map((r) => ({ label: r.label, value: r.code }));

  const reload = () => actionRef.current?.reload();

  const openCreate = () => setFormState({ mode: 'create' });

  const openDetail = async (record: UserVO, mode: 'edit' | 'view') => {
    try {
      const detail = await detailUser(record.num);
      setFormState({ mode, user: detail });
    } catch (e) {
      message.error((e as Error).message || '获取用户详情失败');
    }
  };

  const handleSubmit = (record: UserVO) => {
    if (record.status !== 'DRAFT') {
      message.error('仅草稿态用户允许提交');
      return;
    }
    confirm({
      title: '确认提交该草稿用户？',
      icon: <ExclamationCircleOutlined />,
      content: '提交后用户状态将变为启用。',
      okText: '提交',
      cancelText: '取消',
      onOk: async () => {
        try {
          await submitUser(record.num);
          message.success('提交成功，用户已启用');
          reload();
        } catch (e) {
          message.error((e as Error).message || '提交失败');
        }
      },
    });
  };

  const handleDelete = (record: UserVO) => {
    if (record.status !== 'DRAFT') {
      message.error('仅草稿态用户允许删除');
      return;
    }
    confirm({
      title: '确认删除该草稿用户？',
      icon: <ExclamationCircleOutlined />,
      content: '删除后不可恢复。',
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteUser(record.num);
          message.success('删除成功');
          reload();
        } catch (e) {
          message.error((e as Error).message || '删除失败');
        }
      },
    });
  };

  const handleChangeStatus = (record: UserVO, target: 'ENABLED' | 'DISABLED') => {
    const expected: UserStatus = target === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    const action = target === 'ENABLED' ? '启用' : '禁用';
    if (record.status !== expected) {
      message.error(`仅${STATUS_TEXT[expected]}态用户允许${action}`);
      return;
    }
    confirm({
      title: `确认${action}该用户？`,
      icon: <ExclamationCircleOutlined />,
      okText: action,
      cancelText: '取消',
      onOk: async () => {
        try {
          if (target === 'ENABLED') {
            await enableUser(record.num);
          } else {
            await disableUser(record.num);
          }
          message.success(`${action}成功`);
          reload();
        } catch (e) {
          message.error((e as Error).message || `${action}失败`);
        }
      },
    });
  };

  const columns: ProColumns<UserVO>[] = [
    {
      title: '业务编码',
      dataIndex: 'num',
      width: 230,
      copyable: true,
      ellipsis: true,
      formItemProps: { name: 'keyword' },
      fieldProps: { placeholder: '业务编码 / 姓名 / 邮箱 / 手机号' },
      title_search: '关键词',
    } as ProColumns<UserVO>,
    {
      title: '姓名',
      dataIndex: 'name',
      width: 110,
      search: false,
    },
    {
      title: '角色',
      dataIndex: 'role',
      valueType: 'select',
      fieldProps: { options: roleSelectOptions, allowClear: true },
      render: (_, record) => (
        <span className="role-tag-list">
          {record.roles.map((role) => (
            <Tag key={role.code} color={role.code === 'ADMIN' ? 'blue' : 'green'}>
              {role.label}
            </Tag>
          ))}
        </span>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      ellipsis: true,
      search: false,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 130,
      search: false,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueType: 'select',
      valueEnum: {
        DRAFT: { text: '草稿' },
        ENABLED: { text: '启用' },
        DISABLED: { text: '禁用' },
      },
      render: (_, record) => (
        <Tag color={STATUS_COLOR[record.status]}>{STATUS_TEXT[record.status]}</Tag>
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      search: false,
      ellipsis: true,
    },
    {
      title: '操作',
      valueType: 'option',
      width: 220,
      render: (_, record) => {
        if (record.status === 'DRAFT') {
          return [
            <a key="edit" onClick={() => openDetail(record, 'edit')}>编辑</a>,
            <a key="submit" onClick={() => handleSubmit(record)}>提交</a>,
            <Typography.Link key="delete" type="danger" onClick={() => handleDelete(record)}>
              删除
            </Typography.Link>,
          ];
        }
        if (record.status === 'ENABLED') {
          return [
            <a key="view" onClick={() => openDetail(record, 'view')}>查看</a>,
            <a key="disable" onClick={() => handleChangeStatus(record, 'DISABLED')}>禁用</a>,
            <a key="reset" onClick={() => setResetUser(record)}>重置密码</a>,
          ];
        }
        return [
          <a key="view" onClick={() => openDetail(record, 'view')}>查看</a>,
          <a key="enable" onClick={() => handleChangeStatus(record, 'ENABLED')}>启用</a>,
          <a key="reset" onClick={() => setResetUser(record)}>重置密码</a>,
        ];
      },
    },
  ];

  const formReadonly = formState?.mode === 'view';
  const formIsCreate = formState?.mode === 'create';

  return (
    <PageContainer title="用户管理" subTitle="平台级功能，仅管理员角色用户可访问">
      <ProTable<UserVO>
        actionRef={actionRef}
        rowKey="num"
        columns={columns}
        search={{ labelWidth: 90 }}
        pagination={{ pageSize: 10, showSizeChanger: true }}
        request={async (params) => {
          try {
            const result = await pageUsers({
              keyword: (params.num as string) || undefined,
              role: (params.role as string) || undefined,
              status: (params.status as string) || undefined,
              pageNo: params.current ?? 1,
              pageSize: params.pageSize ?? 10,
            });
            return {
              data: result.records || [],
              total: result.total ?? 0,
              success: true,
            };
          } catch (e) {
            message.error((e as Error).message || '查询失败');
            return { data: [], total: 0, success: false };
          }
        }}
        toolBarRender={() => [
          <Button key="new" type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新增
          </Button>,
        ]}
      />

      <ModalForm
        key={formState ? `${formState.mode}-${formState.user?.num ?? 'new'}` : 'closed'}
        title={
          formIsCreate ? '新增用户' : formReadonly ? '查看用户' : '编辑用户信息'
        }
        open={!!formState}
        modalProps={{ destroyOnClose: true, onCancel: () => setFormState(null), maskClosable: false }}
        initialValues={
          formState?.user
            ? {
                num: formState.user.num,
                email: formState.user.email,
                phone: formState.user.phone,
                name: formState.user.name,
                roles: formState.user.roles.map((r) => r.code),
                remark: formState.user.remark,
                statusLabel: STATUS_TEXT[formState.user.status],
              }
            : { roles: ['MEMBER'], statusLabel: STATUS_TEXT.DRAFT }
        }
        submitter={
          formReadonly
            ? false
            : { searchConfig: { submitText: '保存', resetText: '取消' } }
        }
        onFinish={async (values) => {
          try {
            if (formIsCreate) {
              await createUser({
                email: values.email,
                phone: values.phone || undefined,
                name: values.name,
                roles: values.roles,
                remark: values.remark || undefined,
              });
              message.success('新增成功');
            } else if (formState?.user) {
              if (formState.user.status !== 'DRAFT') {
                message.error('仅草稿态用户允许保存');
                return false;
              }
              await saveUser({
                userNum: formState.user.num,
                email: values.email,
                phone: values.phone || undefined,
                name: values.name,
                roles: values.roles,
                remark: values.remark || undefined,
              });
              message.success('保存成功');
            }
            setFormState(null);
            reload();
            return true;
          } catch (e) {
            message.error((e as Error).message || '操作失败');
            return false;
          }
        }}
      >
        {!formIsCreate && (
          <ProFormText name="num" label="业务编码" readonly />
        )}
        <ProFormText
          name="email"
          label="邮箱"
          readonly={formReadonly}
          rules={[
            { required: true, message: '请输入邮箱' },
            { type: 'email', message: '请输入正确的邮箱格式' },
          ]}
        />
        <ProFormText
          name="phone"
          label="手机号"
          readonly={formReadonly}
          rules={[{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式' }]}
        />
        <ProFormText
          name="name"
          label="姓名"
          readonly={formReadonly}
          rules={[{ required: true, message: '请输入姓名' }]}
        />
        <ProFormCheckbox.Group
          name="roles"
          label="角色"
          disabled={formReadonly}
          options={roleSelectOptions}
          rules={[{ required: true, message: '请至少选择一个角色' }]}
        />
        {!formIsCreate && <ProFormText name="statusLabel" label="状态" readonly />}
        <ProFormTextArea
          name="remark"
          label="备注"
          readonly={formReadonly}
          fieldProps={{ maxLength: 200, showCount: true }}
          rules={[{ max: 200, message: '备注不能超过 200 字' }]}
        />
      </ModalForm>

      <ModalForm
        title="重置密码"
        open={!!resetUser}
        modalProps={{ destroyOnClose: true, onCancel: () => setResetUser(null), maskClosable: false }}
        submitter={{ searchConfig: { submitText: '确认重置', resetText: '取消' } }}
        onFinish={async (values) => {
          if (values.newPassword !== values.confirmPassword) {
            message.error('两次输入的密码不一致');
            return false;
          }
          if (!resetUser) return false;
          try {
            await resetPassword({
              userNum: resetUser.num,
              newPassword: values.newPassword,
              confirmPassword: values.confirmPassword,
            });
            message.success('密码已重置');
            setResetUser(null);
            return true;
          } catch (e) {
            message.error((e as Error).message || '重置失败');
            return false;
          }
        }}
      >
        <Space direction="vertical" style={{ marginBottom: 16 }}>
          <Typography.Text>用户：{resetUser?.name}</Typography.Text>
          <Typography.Text>编码：{resetUser?.num}</Typography.Text>
          <Typography.Text>邮箱：{resetUser?.email}</Typography.Text>
        </Space>
        <ProFormText.Password
          name="newPassword"
          label="新密码"
          rules={[
            { required: true, message: '请输入新密码' },
            { min: 8, message: '密码至少 8 位' },
            { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码至少 8 位且必须包含字母和数字' },
          ]}
        />
        <ProFormText.Password
          name="confirmPassword"
          label="确认新密码"
          rules={[{ required: true, message: '请确认新密码' }]}
        />
      </ModalForm>
    </PageContainer>
  );
}
