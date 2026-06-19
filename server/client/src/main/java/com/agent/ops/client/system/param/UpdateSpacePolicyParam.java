package com.agent.ops.client.system.param;

import com.agent.ops.facade.request.CommonRequest;

/**
 * 更新空间策略入参。
 */
public class UpdateSpacePolicyParam extends CommonRequest {
    /**
     * 每用户配额。
     */
    public Integer quotaPerUser;

    /**
     * 命名规则正则。
     */
    public String namingRegex;
}
