package com.agent.ops.infra.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 审计日志表持久化对象。
 */
@TableName("audit_logs")
public class AuditLogEntity {
    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    /**
     * 业务编码。
     */
    @TableField("num")
    public String num;

    /**
     * 模块。
     */
    @TableField("module")
    public String module;

    /**
     * 事件。
     */
    @TableField("action")
    public String action;

    /**
     * 操作人业务编码。
     */
    @TableField("operator_code")
    public String operatorCode;

    /**
     * 目标资源业务编码。
     */
    @TableField("target_num")
    public String targetNum;

    /**
     * 明细 JSON。
     */
    @TableField("detail_json")
    public String detailJson;

    /**
     * 创建人业务编码。
     */
    @TableField("create_no")
    public String createNo;

    /**
     * 更新人业务编码。
     */
    @TableField("update_no")
    public String updateNo;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    public LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField("update_time")
    public LocalDateTime updateTime;

    /**
     * 软删除标识。
     */
    @TableField("is_deleted")
    public Integer isDeleted;

    public Long getId() { return id; }
    public String getNum() { return num; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getOperatorCode() { return operatorCode; }
    public String getTargetNum() { return targetNum; }
    public String getDetailJson() { return detailJson; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
