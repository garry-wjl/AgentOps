import { Tooltip } from 'antd';

/**
 * 列表单元格统一截断渲染：
 * - 文本 ≤ maxLen（默认 30 字符）：原样展示
 * - 文本 > maxLen：截断 + 悬停 tooltip 展示完整内容
 *
 * 用于「描述」「备注」等长文本字段。「编码」「名称」类标识字段也通过此函数兜底，
 * 因为业务编码格式固定 23 字符、名称多数较短，几乎不会触发截断。
 */
export function ellipsisCell(text: string | number | undefined | null, maxLen = 30): React.ReactNode {
  if (text === null || text === undefined || text === '') return '—';
  const s = String(text);
  if (s.length <= maxLen) return s;
  return (
    <Tooltip title={s} placement="topLeft">
      <span>{s.slice(0, maxLen) + '…'}</span>
    </Tooltip>
  );
}
