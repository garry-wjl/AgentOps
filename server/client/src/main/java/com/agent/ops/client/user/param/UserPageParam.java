package com.agent.ops.client.user.param;

/**
 * 用户分页查询请求参数。
 */
public class UserPageParam {
    public String keyword;
    public String status;
    public String role;
    public int pageNo = 1;
    public int pageSize = 10;
}