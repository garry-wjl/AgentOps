package com.agent.ops.infra.skill.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.skill.enums.SkillVersionStatus;
import com.agent.ops.domain.skill.SkillVersionAggregate;
import com.agent.ops.domain.skill.factory.SkillVersionFactory;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.domain.skill.repository.SkillVersionRepository;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SkillVersionFactoryImpl implements SkillVersionFactory {
    @Resource
    private SkillVersionRepository skillVersionRepository;

    @Resource
    private SkillGateway skillGateway;

    @Resource
    private DomainEventPublisher domainEventPublisher;

    @Override
    public SkillVersionAggregate create(String skillCode, String versionNo, String skillMdContent) {
        Assert.notBlank(skillCode, "skillCode 不能为空");
        SkillVersionAggregate a = new SkillVersionAggregate(skillVersionRepository, skillGateway, domainEventPublisher);
        a.setSkillCode(skillCode);
        a.setVersionNo(versionNo);
        a.setSkillMdContent(skillMdContent);
        a.setStatus(SkillVersionStatus.DRAFT);
        return a;
    }

    @Override
    public SkillVersionAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        SkillVersionAggregate a = skillVersionRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(skillVersionRepository);
        a.setGateway(skillGateway);
        a.setEventPublisher(domainEventPublisher);
        return a;
    }
}
