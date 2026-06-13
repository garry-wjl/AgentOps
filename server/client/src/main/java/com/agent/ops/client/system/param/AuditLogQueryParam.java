package com.agent.ops.client.system.param;

import com.agent.ops.facade.common.page.PageQuery;
import com.agent.ops.facade.request.CommonRequest;

import java.time.LocalDateTime;

/**
 * 审计日志查询入参。
 */
public class AuditLogQueryParam extends CommonRequest {
    /**
     * 模块过滤。
     */
    public String module;

    /**
     * 事件过滤。
     */
    public String action;

    /**
     * 操作人业务编码过滤。
     */
    public String operatorCodeFilter;

    /**
     * 目标资源业务编码过滤。
     */
    public String targetNum;

    /**
     * 起始时间。
     */
    public LocalDateTime fromTime;

    /**
     * 截止时间。
     */
    public LocalDateTime toTime;

    /**
     * 分页。
     */
    public PageQuery pageQuery;
}
