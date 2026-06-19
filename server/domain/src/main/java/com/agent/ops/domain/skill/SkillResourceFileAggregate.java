package com.agent.ops.domain.skill;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.enums.FileType;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.domain.skill.repository.SkillResourceFileRepository;
import com.agent.ops.facade.domain.DomainEntity;

/**
 * Skill 资源文件聚合根。仅 save / delete。
 */
public class SkillResourceFileAggregate extends DomainEntity {
    private String skillVersionCode;
    private String path;
    private FileType type;
    private String content;
    private Long sizeBytes;

    private SkillResourceFileRepository repository;
    private SkillGateway gateway;

    public SkillResourceFileAggregate() { }

    public SkillResourceFileAggregate(SkillResourceFileRepository repository, SkillGateway gateway) {
        this.repository = repository;
        this.gateway = gateway;
    }

    @Override
    public void save(String operatorCode) {
        Assert.notNull(repository, "repository 不能为空");
        Assert.notNull(gateway, "gateway 不能为空");
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateResourceFileCode());
        }
        if (sizeBytes == null) {
            sizeBytes = content == null ? 0L : (long) content.getBytes().length;
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
    }

    @Override
    public void delete(String operatorCode) {
        Assert.notNull(repository, "repository 不能为空");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(skillVersionCode, "skillVersionCode 不能为空");
        Assert.notBlank(path, "path 不能为空");
        Assert.notNull(type, "type 不能为空");
        // 不允许 path 等于 Skill.MD（保留给版本主体）
        Assert.isTrue(!StrUtil.equalsIgnoreCase(path, "Skill.MD"),
                "资源文件树不允许包含 Skill.MD（请在版本 skillMdContent 中维护）");
        // 单文件 ≤ 10MB
        if (sizeBytes != null) {
            Assert.isTrue(sizeBytes <= 10L * 1024 * 1024, "单文件大小不能超过 10MB");
        }
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByPath(skillVersionCode, path, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("path 已存在: " + path);
        }
    }

    public String getSkillVersionCode() { return skillVersionCode; }
    public void setSkillVersionCode(String skillVersionCode) { this.skillVersionCode = skillVersionCode; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public FileType getType() { return type; }
    public void setType(FileType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public void setRepository(SkillResourceFileRepository repository) { this.repository = repository; }
    public void setGateway(SkillGateway gateway) { this.gateway = gateway; }
}
