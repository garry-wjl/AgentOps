package com.agent.ops.infra.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Prompt 持久化对象。
 */
@TableName("prompts")
public class PromptEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num")
    public String num;
    @TableField("space_code")
    public String spaceCode;
    @TableField("name")
    public String name;
    @TableField("`key`")
    public String key;
    @TableField("content")
    public String content;
    @TableField("variables_json")
    public String variablesJson;
    @TableField("remark")
    public String remark;
    @TableField("status")
    public Integer status;
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
    public String getKey() { return key; }
    public String getContent() { return content; }
    public String getVariablesJson() { return variablesJson; }
    public String getRemark() { return remark; }
    public Integer getStatus() { return status; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
