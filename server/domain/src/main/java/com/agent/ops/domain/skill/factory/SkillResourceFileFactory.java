package com.agent.ops.domain.skill.factory;

import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.domain.skill.SkillResourceFileAggregate;

public interface SkillResourceFileFactory {
    SkillResourceFileAggregate create(String versionCode, String path, FileType type, String content);
    SkillResourceFileAggregate createByNum(String num);
}
