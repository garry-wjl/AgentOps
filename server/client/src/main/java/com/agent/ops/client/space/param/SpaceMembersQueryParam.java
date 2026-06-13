package com.agent.ops.client.space.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间成员列表分页查询入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceMembersQueryParam extends CommonRequest {
    /**
     * 空间业务编码。
     */
    public String spaceCode;

    /**
     * 关键字（按用户名/邮箱/手机号模糊匹配）。
     */
    public String keyword;

    /**
     * 分页参数。
     */
    public PageQuery pageQuery;
}
