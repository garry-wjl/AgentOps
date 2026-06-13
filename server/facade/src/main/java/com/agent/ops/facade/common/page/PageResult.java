package com.agent.ops.facade.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * 查询接口通用分页结果。
 *
 * @param <T> 分页记录元素类型
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PageResult<T> {
    /**
     * 匹配查询条件的总记录数。
     */
    private Long total;

    /**
     * 当前页码。
     */
    private Integer pageNo;

    /**
     * 当前每页记录数。
     */
    private Integer pageSize;

    /**
     * 当前页记录列表。
     */
    private List<T> records;

    /**
     * 构建空分页结果。
     *
     * @param pageNo 当前页码
     * @param pageSize 当前每页记录数
     * @param <T> 分页记录元素类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNo, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.total = 0L;
        result.pageNo = pageNo;
        result.pageSize = pageSize;
        result.records = Collections.emptyList();
        return result;
    }

    /**
     * 根据查询输出构建分页结果。
     *
     * @param total 匹配查询条件的总记录数
     * @param pageNo 当前页码
     * @param pageSize 当前每页记录数
     * @param records 当前页记录列表
     * @param <T> 分页记录元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long total, Integer pageNo, Integer pageSize, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.total = total == null ? 0L : total;
        result.pageNo = pageNo;
        result.pageSize = pageSize;
        result.records = records == null ? Collections.emptyList() : records;
        return result;
    }
}
