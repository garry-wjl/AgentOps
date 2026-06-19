package com.agent.ops.client.system.dto;


/**
 * 空间策略 DTO。
 */
public class SpacePolicyDTO {
    /**
     * 每用户可创建的空间配额。
     */
    public Integer quotaPerUser;

    /**
     * 空间命名规则正则。
     */
    public String namingRegex;
}
