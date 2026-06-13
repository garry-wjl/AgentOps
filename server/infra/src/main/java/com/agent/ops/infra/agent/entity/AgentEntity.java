package com.agent.ops.infra.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("agents")
public class AgentEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num") public String num;
    @TableField("space_code") public String spaceCode;
    @TableField("name") public String name;
    @TableField("display_name") public String displayName;
    @TableField("description") public String description;
    @TableField("current_version_no") public String currentVersionNo;
    @TableField("status") public Integer status;
    @TableField("tags_json") public String tagsJson;
    @TableField("remark") public String remark;
    @TableField("create_no") public String createNo;
    @TableField("update_no") public String updateNo;
    @TableField("create_time") public LocalDateTime createTime;
    @TableField("update_time") public LocalDateTime updateTime;
    @TableField("is_deleted") public Integer isDeleted;

    public Long getId() { return id; }
    public String getNum() { return num; }
    public String getSpaceCode() { return spaceCode; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getCurrentVersionNo() { return currentVersionNo; }
    public Integer getStatus() { return status; }
    public String getTagsJson() { return tagsJson; }
    public String getRemark() { return remark; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
