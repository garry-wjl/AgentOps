package com.agent.ops.infra.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("tools")
public class ToolEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num")
    public String num;
    @TableField("space_code")
    public String spaceCode;
    @TableField("name")
    public String name;
    @TableField("type")
    public String type;
    @TableField("sub_type")
    public String subType;
    @TableField("description")
    public String description;
    @TableField("tags_json")
    public String tagsJson;
    @TableField("config_json")
    public String configJson;
    @TableField("status")
    public Integer status;
    @TableField("remark")
    public String remark;
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
    public String getType() { return type; }
    public String getSubType() { return subType; }
    public String getDescription() { return description; }
    public String getTagsJson() { return tagsJson; }
    public String getConfigJson() { return configJson; }
    public Integer getStatus() { return status; }
    public String getRemark() { return remark; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
