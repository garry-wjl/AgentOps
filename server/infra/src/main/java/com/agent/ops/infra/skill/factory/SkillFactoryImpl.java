package com.agent.ops.infra.skill.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.skill.enums.SkillStatus;
import com.agent.ops.domain.skill.SkillAggregate;
import com.agent.ops.domain.skill.factory.SkillFactory;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.domain.skill.repository.SkillRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillFactoryImpl implements SkillFactory {
    @Resource
    private SkillRepository skillRepository;

    @Resource
    private SkillGateway skillGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SkillAggregate create(String spaceCode, String name, String description, List<String> tags, String remark) {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        SkillAggregate a = new SkillAggregate(skillRepository, skillGateway, domainEventPublisher);
        a.setSpaceCode(spaceCode);
        a.setName(name);
        a.setDescription(description);
        a.setTags(tags);
        a.setRemark(remark);
        a.setStatus(SkillStatus.DRAFT);
        return a;
    }

    @Override
    public SkillAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        SkillAggregate a = skillRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(skillRepository);
        a.setGateway(skillGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
