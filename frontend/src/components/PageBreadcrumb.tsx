import { Breadcrumb } from 'antd';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

export interface BreadcrumbItem {
  /** 链接，可选；最后一项通常不传。 */
  to?: string;
  /** 显示文案。 */
  title: ReactNode;
}

/**
 * 页面级面包屑。
 *
 * 设计上放在「内容白色卡片的外侧」——即页面的灰色背景区，
 * 用于列表页点开"详情/编辑"等子页面时，给用户一个清晰的层级提示与返回入口。
 */
export default function PageBreadcrumb({ items }: { items: BreadcrumbItem[] }) {
  return (
    <div className="page-breadcrumb">
      <Breadcrumb
        items={items.map((it, idx) => {
          const isLast = idx === items.length - 1;
          return {
            title: it.to && !isLast ? <Link to={it.to}>{it.title}</Link> : it.title,
          };
        })}
      />
    </div>
  );
}
