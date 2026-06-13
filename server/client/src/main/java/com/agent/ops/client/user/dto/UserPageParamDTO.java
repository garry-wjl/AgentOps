package com.agent.ops.client.user.dto;

/**
 * 用户分页查询参数。
 */
public class UserPageParamDTO {
    /**
     * 关键词，支持用户编码、姓名、邮箱、手机号。
     */
    public String keyword;

    /**
     * 用户状态筛选。
     */
    public String status;

    /**
     * 用户角色筛选。
     */
    public String role;

    /**
     * 页码。
     */
    public Integer pageNo;

    /**
     * 每页记录数。
     */
    public Integer pageSize;

    /**
     * 操作人标识。
     */
    public String operatorCode;
}
