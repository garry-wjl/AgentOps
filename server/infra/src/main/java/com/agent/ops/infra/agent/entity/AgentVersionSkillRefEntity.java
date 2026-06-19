package com.agent.ops.infra.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("agent_version_skill_refs")
public class AgentVersionSkillRefEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("agent_version_code") public String agentVersionCode;
    @TableField("skill_code") public String skillCode;
    @TableField("create_no") public String createNo;
    @TableField("update_no") public String updateNo;
    @TableField("create_time") public LocalDateTime createTime;
    @TableField("update_time") public LocalDateTime updateTime;
    @TableField("is_deleted") public Integer isDeleted;

    public Long getId() { return id; }
    public String getAgentVersionCode() { return agentVersionCode; }
    public String getSkillCode() { return skillCode; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
