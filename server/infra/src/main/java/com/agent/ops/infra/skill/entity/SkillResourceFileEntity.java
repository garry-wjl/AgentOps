package com.agent.ops.infra.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("skill_resource_files")
public class SkillResourceFileEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num") public String num;
    @TableField("skill_version_code") public String skillVersionCode;
    @TableField("path") public String path;
    @TableField("type") public Integer type;
    @TableField("content") public String content;
    @TableField("size_bytes") public Long sizeBytes;
    @TableField("create_no") public String createNo;
    @TableField("update_no") public String updateNo;
    @TableField("create_time") public LocalDateTime createTime;
    @TableField("update_time") public LocalDateTime updateTime;
    @TableField("is_deleted") public Integer isDeleted;

    public Long getId() { return id; }
    public String getNum() { return num; }
    public String getSkillVersionCode() { return skillVersionCode; }
    public String getPath() { return path; }
    public Integer getType() { return type; }
    public String getContent() { return content; }
    public Long getSizeBytes() { return sizeBytes; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
