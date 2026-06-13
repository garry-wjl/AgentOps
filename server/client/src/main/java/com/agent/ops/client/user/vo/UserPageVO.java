package com.agent.ops.client.user.vo;

import java.util.List;

/**
 * 用户分页视图对象。
 */
public class UserPageVO {
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
    public List<UserVO> records;
}