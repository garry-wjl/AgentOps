package com.agent.ops.domain.system.factory;

import com.agent.ops.domain.system.SystemSettingAggregate;

/**
 * 系统设置聚合工厂。
 */
public interface SystemSettingFactory {
    /**
     * 创建新的系统设置聚合（首启 seed 用）。
     *
     * @param category    分类
     * @param settingJson JSON 字符串
     * @return 聚合
     */
    SystemSettingAggregate create(String category, String settingJson);

    /**
     * 按业务编码加载已有聚合。
     *
     * @param num 业务编码
     * @return 聚合，不存在返回 null
     */
    SystemSettingAggregate createByNum(String num);

    /**
     * 按分类加载已有聚合（应用层 update 入口使用）。
     *
     * @param category 分类
     * @return 聚合，不存在返回 null
     */
    SystemSettingAggregate createByCategory(String category);
}
