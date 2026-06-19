package com.agent.ops.domain.model.factory;

import com.agent.ops.domain.model.ModelAggregate;

/**
 * 模型聚合工厂。
 */
public interface ModelFactory {
    /**
     * 新建草稿模型聚合。
     *
     * @param spaceCode 所属空间
     * @param name      名称
     * @param modelId   模型标识
     * @param baseUrl   Base URL
     * @param apiKey    明文 API Key
     * @param remark    备注
     * @return 聚合
     */
    ModelAggregate create(String spaceCode, String name, String modelId, String baseUrl, String apiKey, String remark);

    /**
     * 按业务编码加载。
     *
     * @param num 业务编码
     * @return 聚合
     */
    ModelAggregate createByNum(String num);
}
