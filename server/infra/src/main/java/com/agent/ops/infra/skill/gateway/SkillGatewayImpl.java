package com.agent.ops.infra.skill.gateway;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SkillGatewayImpl implements SkillGateway {
    /**
     * frontmatter 解析正则：匹配文件开头的 `---\n...\n---\n`。
     */
    private static final Pattern FRONTMATTER_BLOCK = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    /**
     * 单字段 `name: xxx` 解析正则。
     */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(.*?)\\s*$", Pattern.MULTILINE);

    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Override
    public String generateSkillCode() {
        return bizCodeGenerator.generate(BizCodePrefix.SKILL);
    }

    @Override
    public String generateSkillVersionCode() {
        return bizCodeGenerator.generate(BizCodePrefix.SKILL_VERSION);
    }

    @Override
    public String generateResourceFileCode() {
        // 资源文件用 SK 前缀+一个区分符；本期与 SKILL 共用前缀（不影响业务）
        return bizCodeGenerator.generate(BizCodePrefix.SKILL);
    }

    @Override
    public Frontmatter parseFrontmatter(String skillMd) {
        if (StrUtil.isBlank(skillMd)) {
            return null;
        }
        Matcher block = FRONTMATTER_BLOCK.matcher(skillMd);
        if (!block.find()) {
            return null;
        }
        String body = block.group(1);
        String name = null, description = null, version = null;
        Matcher field = FIELD_PATTERN.matcher(body);
        while (field.find()) {
            String key = field.group(1);
            String value = field.group(2);
            if ("name".equals(key)) {
                name = value;
            } else if ("description".equals(key)) {
                description = value;
            } else if ("version".equals(key)) {
                version = value;
            }
        }
        return new Frontmatter(name, description, version);
    }
}
