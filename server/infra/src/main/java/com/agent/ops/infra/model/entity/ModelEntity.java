package com.agent.ops.infra.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 模型表持久化对象。
 */
@TableName("models")
public class ModelEntity {
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @TableField("num")
    public String num;
    @TableField("space_code")
    public String spaceCode;
    @TableField("name")
    public String name;
    @TableField("model_id")
    public String modelId;
    @TableField("base_url")
    public String baseUrl;
    @TableField("api_key_cipher")
    public String apiKeyCipher;
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
    public String getModelId() { return modelId; }
    public String getBaseUrl() { return baseUrl; }
    public String getApiKeyCipher() { return apiKeyCipher; }
    public String getRemark() { return remark; }
    public Integer getStatus() { return status; }
    public String getCreateNo() { return createNo; }
    public String getUpdateNo() { return updateNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
}
