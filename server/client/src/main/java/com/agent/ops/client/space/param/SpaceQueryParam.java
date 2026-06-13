package com.agent.ops.client.space.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * "我的空间"卡片分页查询入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryParam extends CommonRequest {
    /**
     * 关键字（按名称模糊匹配）。
     */
    public String keyword;

    /**
     * 分页参数。
     */
    public PageQuery pageQuery;
}
