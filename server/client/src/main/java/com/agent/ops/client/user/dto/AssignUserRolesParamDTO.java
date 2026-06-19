package com.agent.ops.client.user.dto;

import java.util.List;

/**
 * 分配用户平台角色参数。
 */
public class AssignUserRolesParamDTO {
    /**
     * 用户业务编码。
     */
    public String userNum;

    /**
     * 平台角色编码列表。
     */
    public List<String> roles;

    /**
     * 操作人标识。
     */
    public String operatorCode;
}
