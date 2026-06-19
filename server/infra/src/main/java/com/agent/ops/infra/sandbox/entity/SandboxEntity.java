package com.agent.ops.infra.sandbox.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("sandboxes")
public class SandboxEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num")
    public String num;
    @TableField("space_code")
    public String spaceCode;
    @TableField("name")
    public String name;
    @TableField("image")
    public String image;
    @TableField("base_url_override")
    public String baseUrlOverride;
    @TableField("remark")
    public String remark;
    @TableField("status")
    public Integer status;
    @TableField("last_status_reason")
    public String lastStatusReason;
    @TableField("last_heartbeat_time")
    public LocalDateTime lastHeartbeatTime;
    @TableField("create_no")
    public String createNo;
    @TableField("update_no")
    public String updateNo;
    @TableField("create_time")
    public LocalDateTime createTime;
    @TableField("update_time")
    public LocalDateTime updateTime;
    @TableField("is_deleted")
    public Integer isDeleted;

    public Long getId() { return id; }
    public String getNum() { return num; }
    public String getSpaceCode() { return spaceCode; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public String getBaseUrlOverride() { return baseUrlOverride; }
    public String getRemark() { return remark; }
    public Integer getStatus() { return status; }
    public String getLastStatusReason() { return lastStatusReason; }
    public LocalDateTime getLastHeartbeatTime() { return lastHeartbeatTime; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
