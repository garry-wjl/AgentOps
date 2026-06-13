package com.agent.ops.domain.system.repository;

import com.agent.ops.domain.system.SystemSettingAggregate;

/**
 * 系统设置聚合仓储契约。
 */
public interface SystemSettingRepository {
    /**
     * 持久化（新建或更新）。
     *
     * @param aggregate 系统设置聚合
     */
    void save(SystemSettingAggregate aggregate);

    /**
     * 按业务编码查询。
     *
     * @param num 业务编码
     * @return 聚合，不存在返回 null
     */
    SystemSettingAggregate findByNum(String num);

    /**
     * 按分类查询。
     *
     * @param category 分类编码
     * @return 聚合，不存在返回 null
     */
    SystemSettingAggregate findByCategory(String category);

    /**
     * 按业务编码软删除。
     *
     * @param num          业务编码
     * @param operatorCode 当前操作人
     */
    void deleteByNum(String num, String operatorCode);
}
