package com.agent.ops.infra.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("agent_versions")
public class AgentVersionEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num") public String num;
    @TableField("agent_code") public String agentCode;
    @TableField("version_no") public String versionNo;
    /**
     * 装配快照 JSON（独立 LONGTEXT）。
     */
    @TableField("assembly_snapshot") public String assemblySnapshot;
    @TableField("status") public Integer status;
    @TableField("online_time") public LocalDateTime onlineTime;
    @TableField("offline_time") public LocalDateTime offlineTime;
    @TableField("create_no") public String createNo;
    @TableField("update_no") public String updateNo;
    @TableField("create_time") public LocalDateTime createTime;
    @TableField("update_time") public LocalDateTime updateTime;
    @TableField("is_deleted") public Integer isDeleted;

    public Long getId() { return id; }
    public String getNum() { return num; }
    public String getAgentCode() { return agentCode; }
    public String getVersionNo() { return versionNo; }
    public String getAssemblySnapshot() { return assemblySnapshot; }
    public Integer getStatus() { return status; }
    public LocalDateTime getOnlineTime() { return onlineTime; }
    public LocalDateTime getOfflineTime() { return offlineTime; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
