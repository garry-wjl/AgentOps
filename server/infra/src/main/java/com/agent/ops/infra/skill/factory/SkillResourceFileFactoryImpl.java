package com.agent.ops.infra.skill.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.domain.skill.SkillResourceFileAggregate;
import com.agent.ops.domain.skill.factory.SkillResourceFileFactory;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.domain.skill.repository.SkillResourceFileRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SkillResourceFileFactoryImpl implements SkillResourceFileFactory {
    @Resource
    private SkillResourceFileRepository skillResourceFileRepository;

    @Resource
    private SkillGateway skillGateway;

    @Override
    public SkillResourceFileAggregate create(String versionCode, String path, FileType type, String content) {
        Assert.notBlank(versionCode, "versionCode 不能为空");
        SkillResourceFileAggregate a = new SkillResourceFileAggregate(skillResourceFileRepository, skillGateway);
        a.setSkillVersionCode(versionCode);
        a.setPath(path);
        a.setType(type);
        a.setContent(content);
        return a;
    }

    @Override
    public SkillResourceFileAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        SkillResourceFileAggregate a = skillResourceFileRepository.findByNum(num);
        if (a == null) return null;
        a.setRepository(skillResourceFileRepository);
        a.setGateway(skillGateway);
        return a;
    }
}
