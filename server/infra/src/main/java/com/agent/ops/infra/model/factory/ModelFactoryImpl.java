package com.agent.ops.infra.model.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.model.enums.ModelStatus;
import com.agent.ops.domain.model.ModelAggregate;
import com.agent.ops.domain.model.factory.ModelFactory;
import com.agent.ops.domain.model.gateway.ModelGateway;
import com.agent.ops.domain.model.repository.ModelRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 模型工厂实现。
 */
@Component
public class ModelFactoryImpl implements ModelFactory {
    @Resource
    private ModelRepository modelRepository;

    @Resource
    private ModelGateway modelGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public ModelAggregate create(String spaceCode, String name, String modelId, String baseUrl, String apiKey, String remark) {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        ModelAggregate a = new ModelAggregate(modelRepository, modelGateway, domainEventPublisher);
        a.setSpaceCode(spaceCode);
        a.setName(name);
        a.setModelId(modelId);
        a.setBaseUrl(baseUrl);
        a.setApiKeyPlaintext(apiKey);
        a.setRemark(remark);
        a.setStatus(ModelStatus.DRAFT);
        return a;
    }

    @Override
    public ModelAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        ModelAggregate a = modelRepository.findByNum(num);
        if (a == null) {
            return null;
        }
        a.setRepository(modelRepository);
        a.setGateway(modelGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
