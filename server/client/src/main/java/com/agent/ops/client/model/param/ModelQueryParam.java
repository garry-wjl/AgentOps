package com.agent.ops.client.model.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;

/**
 * 模型分页查询入参。
 */
public class ModelQueryParam extends CommonRequest {
    public String spaceCode;
    public String keyword;
    public String status;
    public PageQuery pageQuery;
}
