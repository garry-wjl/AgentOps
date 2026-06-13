package com.agent.ops.infra.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("skill_versions")
public class SkillVersionEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num") public String num;
    @TableField("skill_code") public String skillCode;
    @TableField("version_no") public String versionNo;
    @TableField("skill_md_content") public String skillMdContent;
    @TableField("status") public Integer status;
    @TableField("publish_time") public LocalDateTime publishTime;
    @TableField("withdraw_time") public LocalDateTime withdrawTime;
    @TableField("create_no") public String createNo;
    @TableField("update_no") public String updateNo;
    @TableField("create_time") public LocalDateTime createTime;
    @TableField("update_time") public LocalDateTime updateTime;
    @TableField("is_deleted") public Integer isDeleted;

    public Long getId() { return id; }
    public String getNum() { return num; }
    public String getSkillCode() { return skillCode; }
    public String getVersionNo() { return versionNo; }
    public String getSkillMdContent() { return skillMdContent; }
    public Integer getStatus() { return status; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public LocalDateTime getWithdrawTime() { return withdrawTime; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
