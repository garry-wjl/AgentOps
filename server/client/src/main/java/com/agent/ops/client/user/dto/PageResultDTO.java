package com.agent.ops.client.user.dto;

import java.util.List;

/**
 * 客户端分页结果数据传输对象。
 *
 * @param <T> 分页记录类型
 */
public class PageResultDTO<T> {
    /**
     * 总记录数。
     */
    public Long total;

    /**
     * 当前页码。
     */
    public Integer pageNo;

    /**
     * 当前每页记录数。
     */
    public Integer pageSize;

    /**
     * 当前页记录列表。
     */
    public List<T> records;
}
