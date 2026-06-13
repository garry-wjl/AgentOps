package com.agent.ops.domain.space.factory;

import com.agent.ops.domain.space.SpaceAggregate;

/**
 * 空间聚合工厂契约。
 */
public interface SpaceFactory {
    /**
     * 根据用户填写字段创建新的空间聚合。owner 自动加入 adminUserCodes。
     *
     * @param name          空间名称
     * @param description   描述
     * @param iconUrl       Logo URL
     * @param ownerUserCode 空间所有者用户业务编码
     * @return 新空间聚合（id 为空，业务编码已生成，status=ENABLED）
     */
    SpaceAggregate create(String name, String description, String iconUrl, String ownerUserCode);

    /**
     * 根据业务编码加载已有空间聚合。
     *
     * @param num 空间业务编码
     * @return 空间聚合
     */
    SpaceAggregate createByNum(String num);
}
