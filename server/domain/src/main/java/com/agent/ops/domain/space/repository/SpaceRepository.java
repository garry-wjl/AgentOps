package com.agent.ops.domain.space.repository;

import com.agent.ops.domain.space.SpaceAggregate;

/**
 * 空间聚合仓储契约。
 */
public interface SpaceRepository {
    /**
     * 持久化空间聚合（新建或更新）。
     *
     * @param space 空间聚合
     */
    void save(SpaceAggregate space);

    /**
     * 根据业务编码删除空间（软删）。
     *
     * @param num          空间业务编码
     * @param operatorCode 当前操作人业务编码
     */
    void deleteByNum(String num, String operatorCode);

    /**
     * 根据业务编码加载空间聚合（自动过滤已删除）。
     *
     * @param num 空间业务编码
     * @return 空间聚合，不存在返回 null
     */
    SpaceAggregate findByNum(String num);

    /**
     * 校验空间名称是否已存在（含软删除过滤）。
     *
     * @param name        空间名称
     * @param excludeNum  排除自检的空间业务编码（编辑场景），null 表示不排除
     * @return 是否已存在
     */
    boolean existsByName(String name, String excludeNum);
}
