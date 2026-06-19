import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

export default function ForbiddenPage() {
  const navigate = useNavigate();
  return (
    <Result
      status="403"
      title="无权限访问该功能"
      subTitle="普通用户不能使用用户管理和系统设置功能。"
      extra={
        <Button type="primary" onClick={() => navigate('/platform/workbench')}>
          返回工作台
        </Button>
      }
    />
  );
}
