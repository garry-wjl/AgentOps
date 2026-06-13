package com.agent.ops.client.space.dto;

import com.agent.ops.client.space.enums.SpaceStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 空间数据传输对象，跨层传递空间快照。
 */
@Data
public class SpaceDTO {
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
     * 状态。
     */
    public SpaceStatus status;

    /**
     * 管理员用户业务编码列表（含 owner）。
     */
    public List<String> adminUserCodes;

    /**
     * 普通成员用户业务编码列表。
     */
    public List<String> memberUserCodes;

    /**
     * 创建时间。
     */
    public LocalDateTime createTime;

    /**
     * 最近更新时间。
     */
    public LocalDateTime updateTime;
}
