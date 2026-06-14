import { ToolOutlined } from '@ant-design/icons';
import { Card, Empty, Typography } from 'antd';

const { Title, Paragraph } = Typography;

interface ComingSoonPageProps {
  title: string;
  description?: string;
}

/**
 * 通用「建设中」占位页：用于 Agent 调试 / Agent 评测 / 记忆管理 / 知识库管理 等
 * 只有菜单先占位、子模块后续承接的入口。
 */
export default function ComingSoonPage({ title, description }: ComingSoonPageProps) {
  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {title}
          </Title>
          {description && (
            <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
              {description}
            </Paragraph>
          )}
        </div>
      </div>
      <Card variant="outlined">
        <Empty
          image={<ToolOutlined style={{ fontSize: 56, color: '#d9d9d9' }} />}
          imageStyle={{ height: 80 }}
          description={
            <span style={{ color: '#999' }}>
              {title} 能力建设中，敬请期待
            </span>
          }
        />
      </Card>
    </div>
  );
}
