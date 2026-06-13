package com.agent.ops.infra.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表持久化对象。
 */
@Data
@TableName("users")
public class UserEntity {
    /**
     * 数据库自增主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    /**
     * 用户业务编码。
     */
    @TableField("num")
    public String num;

    /**
     * 邮箱登录账号，草稿创建时允许为空。
     */
    @TableField("email")
    public String email;

    /**
     * 手机号登录账号，允许为空。
     */
    @TableField("phone")
    public String phone;

    /**
     * 用户姓名或展示名称，草稿创建时允许为空。
     */
    @TableField("name")
    public String name;

    /**
     * 平台角色 JSON 数组，取值包含 ADMIN、MEMBER。
     */
    @TableField("roles")
    public String roles;

    /**
     * 用户生命周期状态，取值为 DRAFT、ENABLED、DISABLED。
     */
    @TableField("status")
    public String status;

    /**
     * 密码安全哈希值。
     */
    @TableField("password_hash")
    public String passwordHash;

    /**
     * 是否已设置密码，1 表示已设置，0 表示未设置。
     */
    @TableField("password_set")
    public Integer passwordSet;

    /**
     * 用户备注，最大 200 字。
     */
    @TableField("remark")
    public String remark;

    /**
     * 基础设施层软删除标识，1 表示已删除，0 表示未删除。
     */
    @TableField("is_deleted")
    public Integer isDeleted;

    /**
     * 创建操作人标识。
     */
    @TableField("create_no")
    public Long createNo;

    /**
     * 更新操作人标识。
     */
    @TableField("update_no")
    public Long updateNo;

    /**
     * 创建时间，精确到毫秒。
     */
    @TableField("create_time")
    public LocalDateTime createTime;

    /**
     * 更新时间，精确到毫秒。
     */
    @TableField("update_time")
    public LocalDateTime updateTime;















    /**
     * 返回id。
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回num。
     *
     * @return num
     */
    public String getNum() {
        return num;
    }

    /**
     * 返回email。
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 返回phone。
     *
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 返回name。
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 返回roles。
     *
     * @return roles
     */
    public String getRoles() {
        return roles;
    }

    /**
     * 返回status。
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 返回passwordHash。
     *
     * @return passwordHash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 返回passwordSet。
     *
     * @return passwordSet
     */
    public Integer getPasswordSet() {
        return passwordSet;
    }

    /**
     * 返回remark。
     *
     * @return remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 返回isDeleted。
     *
     * @return isDeleted
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * 返回updateNo。
     *
     * @return updateNo
     */
    public Long getUpdateNo() {
        return updateNo;
    }

    /**
     * 返回createNo。
     *
     * @return createNo
     */
    public Long getCreateNo() {
        return createNo;
    }

    /**
     * 返回updateTime。
     *
     * @return updateTime
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}