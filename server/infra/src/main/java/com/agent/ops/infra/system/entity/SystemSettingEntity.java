package com.agent.ops.infra.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 系统设置表持久化对象。
 */
@TableName("system_settings")
public class SystemSettingEntity {
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
     * 分类。
     */
    @TableField("category")
    public String category;

    /**
     * 设置 JSON。
     */
    @TableField("setting_json")
    public String settingJson;

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
    public String getCategory() { return category; }
    public String getSettingJson() { return settingJson; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
