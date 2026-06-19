package com.agent.ops.infra.tool.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.domain.tool.ToolAggregate;
import com.agent.ops.domain.tool.factory.ToolFactory;
import com.agent.ops.domain.tool.gateway.ToolGateway;
import com.agent.ops.domain.tool.repository.ToolRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolFactoryImpl implements ToolFactory {
    @Resource
    private ToolRepository toolRepository;

    @Resource
    private ToolGateway toolGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public ToolAggregate create(String spaceCode, String name, ToolType type, ToolSubType subType,
                                String description, List<String> tags, String configJson, String remark) {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notNull(type, "type 不能为空");
        Assert.notNull(subType, "subType 不能为空");
        ToolAggregate a = new ToolAggregate(toolRepository, toolGateway, domainEventPublisher);
        a.setSpaceCode(spaceCode);
        a.setName(name);
        a.setType(type);
        a.setSubType(subType);
        a.setDescription(description);
        a.setTags(tags);
        a.setConfigJson(configJson);
        a.setRemark(remark);
        a.setStatus(ToolStatus.DRAFT);
        return a;
    }

    @Override
    public ToolAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        ToolAggregate a = toolRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(toolRepository);
        a.setGateway(toolGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
