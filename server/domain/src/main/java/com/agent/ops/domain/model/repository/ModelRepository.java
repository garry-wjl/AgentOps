package com.agent.ops.domain.model.repository;

import com.agent.ops.domain.model.ModelAggregate;

/**
 * 模型聚合仓储。
 */
public interface ModelRepository {
    void save(ModelAggregate aggregate);
    ModelAggregate findByNum(String num);
    void deleteByNum(String num, String operatorCode);
    boolean existsByName(String spaceCode, String name, String excludeNum);
    boolean existsByModelId(String spaceCode, String modelId, String excludeNum);
}
