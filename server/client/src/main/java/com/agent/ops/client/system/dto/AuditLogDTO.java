package com.agent.ops.client.system.dto;


import java.time.LocalDateTime;

/**
 * 审计日志 DTO。
 */
public class AuditLogDTO {
    /**
     * 业务编码。
     */
    public String num;

    /**
     * 模块（model/agent/skill 等）。
     */
    public String module;

    /**
     * 事件类型。
     */
    public String action;

    /**
     * 操作人用户业务编码。
     */
    public String operatorCode;

    /**
     * 目标资源业务编码。
     */
    public String targetNum;

    /**
     * 字段差异等明细 JSON。
     */
    public String detailJson;

    /**
     * 创建时间。
     */
    public LocalDateTime createTime;
}
