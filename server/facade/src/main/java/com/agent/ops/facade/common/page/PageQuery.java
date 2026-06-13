package com.agent.ops.facade.common.page;

import lombok.Getter;
import lombok.Setter;

/**
 * 带有默认页码和最大页大小保护的分页查询基类。
 */
@Getter
@Setter
public class PageQuery {
    /**
     * 默认第一页页码。
     */
    private static final int DEFAULT_PAGE_NO = 1;

    /**
     * 默认每页记录数。
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 允许请求的最大每页记录数。
     */
    private static final int MAX_PAGE_SIZE = 200;

    /**
     * 请求页码。
     */
    private Integer pageNo = DEFAULT_PAGE_NO;

    /**
     * 请求每页记录数。
     */
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 返回归一化后的页码。
     *
     * @return 归一化后的页码
     */
    public Integer getPageNo() {
        if (pageNo == null || pageNo < 1) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    /**
     * 返回归一化后的每页记录数。
     *
     * @return 归一化后的每页记录数
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 设置请求页码。
     *
     * @param pageNo 请求页码
     */
    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * 设置请求每页记录数。
     *
     * @param pageSize 请求每页记录数
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
