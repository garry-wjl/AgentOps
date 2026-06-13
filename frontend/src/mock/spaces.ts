/**
 * 空间 Mock 数据 —— 对应《空间管理 PRD》。
 */

export type SpaceRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export interface SpaceMember {
  userNum: string;
  name: string;
  email: string;
  role: SpaceRole;
}

export interface Space {
  id: string;
  num: string;
  name: string;
  remark?: string;
  createdBy: string;
  createdAt: string;
  myRole: 'ADMIN' | 'MEMBER';
  admins: SpaceMember[];
  members: SpaceMember[];
}

export const mockSpaces: Space[] = [
  {
    id: 'sp-001',
    num: 'SP202606131426301234567',
    name: '家庭客服 Agent',
    remark: '家庭场景下的智能客服试验空间',
    createdBy: '张三',
    createdAt: '2026-06-13 14:26',
    myRole: 'ADMIN',
    admins: [
      { userNum: 'US001', name: '张三', email: 'zhangsan@example.com', role: 'OWNER' },
      { userNum: 'US002', name: '李四', email: 'lisi@example.com', role: 'ADMIN' },
    ],
    members: [
      { userNum: 'US003', name: '王五', email: 'wangwu@example.com', role: 'MEMBER' },
      { userNum: 'US004', name: '赵六', email: 'zhaoliu@example.com', role: 'MEMBER' },
      { userNum: 'US005', name: '钱七', email: 'qianqi@example.com', role: 'MEMBER' },
    ],
  },
  {
    id: 'sp-002',
    num: 'SP202606121426301111111',
    name: '办公自动化空间',
    remark: '内部办公自动化',
    createdBy: '李四',
    createdAt: '2026-06-12 09:11',
    myRole: 'MEMBER',
    admins: [{ userNum: 'US002', name: '李四', email: 'lisi@example.com', role: 'OWNER' }],
    members: [
      { userNum: 'US001', name: '张三', email: 'zhangsan@example.com', role: 'MEMBER' },
      { userNum: 'US006', name: '孙八', email: 'sunba@example.com', role: 'MEMBER' },
    ],
  },
  {
    id: 'sp-003',
    num: 'SP202606101426301222222',
    name: '数据分析实验',
    remark: 'BI 报表 + 沙箱代码执行',
    createdBy: '张三',
    createdAt: '2026-06-10 17:42',
    myRole: 'ADMIN',
    admins: [{ userNum: 'US001', name: '张三', email: 'zhangsan@example.com', role: 'OWNER' }],
    members: [
      { userNum: 'US002', name: '李四', email: 'lisi@example.com', role: 'MEMBER' },
    ],
  },
];

/**
 * 根据 ID 获取空间。
 */
export function findSpace(id: string): Space | undefined {
  return mockSpaces.find((s) => s.id === id);
}
