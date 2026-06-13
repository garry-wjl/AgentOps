/**
 * 平台用户 Mock 池 —— 用于「空间编辑」中选择管理员/普通成员等场景。
 * 仅启用态用户可被选中。
 */

export interface PlatformUserOption {
  userNum: string;
  name: string;
  email: string;
}

export const mockPlatformUsers: PlatformUserOption[] = [
  { userNum: 'US001', name: '张三', email: 'zhangsan@example.com' },
  { userNum: 'US002', name: '李四', email: 'lisi@example.com' },
  { userNum: 'US003', name: '王五', email: 'wangwu@example.com' },
  { userNum: 'US004', name: '赵六', email: 'zhaoliu@example.com' },
  { userNum: 'US005', name: '钱七', email: 'qianqi@example.com' },
  { userNum: 'US006', name: '孙八', email: 'sunba@example.com' },
  { userNum: 'US007', name: '周九', email: 'zhoujiu@example.com' },
  { userNum: 'US008', name: '吴十', email: 'wushi@example.com' },
  { userNum: 'US-ME', name: '当前用户', email: 'me@example.com' },
];
