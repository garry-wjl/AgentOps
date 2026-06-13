import { ExperimentOutlined } from '@ant-design/icons';
import { Card, Empty, Typography } from 'antd';

const { Title, Paragraph } = Typography;

/**
 * 调试与评测 —— 占位页（《UI 信息架构与导航规范》本期暂无子项）。
 */
export default function DebugPlaceholderPage() {
  return (
    <div className="page-section">
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>
            调试与评测
          </Title>
          <Paragraph type="secondary" style={{ margin: '6px 0 0' }}>
            该分组将承接：运行记录 (Trace) / Prompt 调试 / 评测集 等能力
          </Paragraph>
        </div>
      </div>
      <Card variant="outlined">
        <Empty
          image={<ExperimentOutlined style={{ fontSize: 56, color: '#d9d9d9' }} />}
          imageStyle={{ height: 80 }}
          description={
            <span style={{ color: '#999' }}>
              调试与评测能力建设中，将逐步开放运行记录、Trace 详情、评测集等子模块。
            </span>
          }
        />
      </Card>
    </div>
  );
}
