package com.agent.ops.infra.space.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 空间表持久化对象。
 * <p>
 * admin_user_codes / member_user_codes 列在 MySQL 中以 JSON 类型存储 String 数组，
 * 由 RepositoryImpl 在 toEntity / toDomain 时显式做 JSON 序列化与反序列化。
 */
@Data
@TableName("spaces")
public class SpaceEntity {
    /**
     * 数据库自增主键。
     */
    @TableId(value = "id", type = IdType.AUTO)
    public Long id;

    /**
     * 空间业务编码（SP+ts+rand）。
     */
    @TableField("num")
    public String num;

    /**
     * 空间名称。
     */
    @TableField("name")
    public String name;

    /**
     * 描述。
     */
    @TableField("description")
    public String description;

    /**
     * Logo URL。
     */
    @TableField("icon_url")
    public String iconUrl;

    /**
     * 空间所有者用户业务编码。
     */
    @TableField("owner_user_code")
    public String ownerUserCode;

    /**
     * 状态：1=ENABLED。
     */
    @TableField("status")
    public Integer status;

    /**
     * 管理员用户业务编码 JSON 字符串数组（含 owner）。
     */
    @TableField("admin_user_codes")
    public String adminUserCodes;

    /**
     * 普通成员用户业务编码 JSON 字符串数组。
     */
    @TableField("member_user_codes")
    public String memberUserCodes;

    /**
     * 创建人用户业务编码。
     */
    @TableField("create_no")
    public String createNo;

    /**
     * 最近更新人用户业务编码。
     */
    @TableField("update_no")
    public String updateNo;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    public LocalDateTime createTime;

    /**
     * 最近更新时间。
     */
    @TableField("update_time")
    public LocalDateTime updateTime;

    /**
     * 软删除标识：0=未删除，1=已删除。
     */
    @TableField("is_deleted")
    public Integer isDeleted;

    /**
     * 返回 id。
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * 返回 num。
     *
     * @return num
     */
    public String getNum() {
        return num;
    }

    /**
     * 返回 name。
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 返回 description。
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 返回 iconUrl。
     *
     * @return iconUrl
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * 返回 ownerUserCode。
     *
     * @return ownerUserCode
     */
    public String getOwnerUserCode() {
        return ownerUserCode;
    }

    /**
     * 返回 status。
     *
     * @return status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 返回 adminUserCodes。
     *
     * @return adminUserCodes JSON 字符串
     */
    public String getAdminUserCodes() {
        return adminUserCodes;
    }

    /**
     * 返回 memberUserCodes。
     *
     * @return memberUserCodes JSON 字符串
     */
    public String getMemberUserCodes() {
        return memberUserCodes;
    }

    /**
     * 返回 createNo。
     *
     * @return createNo
     */
    public String getCreateNo() {
        return createNo;
    }

    /**
     * 返回 updateNo。
     *
     * @return updateNo
     */
    public String getUpdateNo() {
        return updateNo;
    }

    /**
     * 返回 createTime。
     *
     * @return createTime
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 返回 updateTime。
     *
     * @return updateTime
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 返回 isDeleted。
     *
     * @return isDeleted
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }
}
