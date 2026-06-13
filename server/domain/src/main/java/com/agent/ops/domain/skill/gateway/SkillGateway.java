package com.agent.ops.domain.skill.gateway;

/**
 * Skill 领域网关：仅本领域职能（编码生成、frontmatter 解析）。
 */
public interface SkillGateway {
    String generateSkillCode();
    String generateSkillVersionCode();
    String generateResourceFileCode();

    /**
     * 解析 Skill.MD 中的 frontmatter，返回 [name, description, version]，缺失则返回 null。
     *
     * @param skillMd Skill.MD 文本
     * @return Frontmatter，含 name/description/version 三字段
     */
    Frontmatter parseFrontmatter(String skillMd);

    /**
     * Skill.MD frontmatter 表示。
     */
    record Frontmatter(String name, String description, String version) { }
}
