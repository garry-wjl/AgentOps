package com.agent.ops.client.space.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * "我的空间"卡片视图对象。
 */
@Data
public class SpaceCardVO {
    /**
     * 业务编码。
     */
    public String num;

    /**
     * 空间名称。
     */
    public String name;

    /**
     * 描述。
     */
    public String description;

    /**
     * Logo URL。
     */
    public String iconUrl;

    /**
     * 空间所有者用户业务编码。
     */
    public String ownerUserCode;

    /**
     * 当前用户在该空间中的角色：ADMIN / MEMBER。
     */
    public String currentUserRole;

    /**
     * 管理员人数。
     */
    public Integer adminCount;

    /**
     * 普通成员人数。
     */
    public Integer memberCount;

    /**
     * 创建时间。
     */
    public LocalDateTime createTime;

    /**
     * 最近更新时间。
     */
    public LocalDateTime updateTime;
}
