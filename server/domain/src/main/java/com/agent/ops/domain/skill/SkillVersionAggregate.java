package com.agent.ops.domain.skill;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.enums.SkillVersionStatus;
import com.agent.ops.domain.skill.event.SkillEventConstant;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.domain.skill.repository.SkillVersionRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.time.LocalDateTime;

/**
 * Skill 版本聚合根。
 */
public class SkillVersionAggregate extends DomainEntity {
    private String skillCode;
    private String versionNo;
    private String skillMdContent;
    private SkillVersionStatus status;
    private LocalDateTime publishTime;
    private LocalDateTime withdrawTime;

    private SkillVersionRepository repository;
    private SkillGateway gateway;
    private DomainEventPublisher eventPublisher;

    public SkillVersionAggregate() { }

    public SkillVersionAggregate(SkillVersionRepository repository, SkillGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = SkillVersionStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateSkillVersionCode());
        }
        // 校验 frontmatter 三字段一致
        if (StrUtil.isNotBlank(skillMdContent)) {
            SkillGateway.Frontmatter fm = gateway.parseFrontmatter(skillMdContent);
            if (fm != null && fm.version() != null) {
                Assert.isTrue(StrUtil.equals(fm.version(), versionNo),
                        "Skill.MD frontmatter 的 version 与 versionNo 不一致");
            }
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(SkillEventConstant.VERSION_CREATED, operatorCode);
        }
    }

    public void publish(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SkillVersionStatus.DRAFT, "仅草稿态可发布");
        // 把同 Skill 旧 EFFECTIVE 切到 WITHDRAWN
        SkillVersionAggregate old = repository.findEffectiveBySkillCode(skillCode);
        if (old != null && !StrUtil.equals(old.getNum(), getNum())) {
            old.setRepository(repository);
            old.setGateway(gateway);
            old.setEventPublisher(eventPublisher);
            old.withdrawInternal(operatorCode);
        }
        this.status = SkillVersionStatus.EFFECTIVE;
        this.publishTime = LocalDateTimeUtil.now();
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SkillEventConstant.VERSION_PUBLISHED, operatorCode);
    }

    public void withdraw(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SkillVersionStatus.EFFECTIVE, "仅生效态可下架");
        withdrawInternal(operatorCode);
    }

    private void withdrawInternal(String operatorCode) {
        this.status = SkillVersionStatus.WITHDRAWN;
        this.withdrawTime = LocalDateTimeUtil.now();
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SkillEventConstant.VERSION_WITHDRAWN, operatorCode);
    }

    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SkillVersionStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(SkillEventConstant.VERSION_DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(skillCode, "skillCode 不能为空");
        Assert.notBlank(versionNo, "versionNo 不能为空");
        Assert.isTrue(StrUtil.length(versionNo) <= 20, "versionNo 长度不能超过 20 字符");
        Assert.notBlank(skillMdContent, "skillMdContent 不能为空");
        Assert.notNull(status, "status 不能为空");
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByVersionNo(skillCode, versionNo, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("版本号已存在");
        }
    }

    private void assertCollaboratorsReady() {
        Assert.notNull(repository, "repository 不能为空");
        Assert.notNull(gateway, "gateway 不能为空");
        Assert.notNull(eventPublisher, "eventPublisher 不能为空");
    }

    private void publishEvent(String eventType, String operatorCode) {
        DomainEventDTO event = new DomainEventDTO();
        event.setEventId(IdUtil.fastSimpleUUID());
        event.setEventType(eventType);
        event.setBusinessNum(getNum());
        event.setOccurredAt(LocalDateTimeUtil.now());
        event.setOperatorCode(operatorCode);
        event.setPayload(this);
        eventPublisher.publish(event);
    }

    public String getSkillCode() { return skillCode; }
    public void setSkillCode(String skillCode) { this.skillCode = skillCode; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public String getSkillMdContent() { return skillMdContent; }
    public void setSkillMdContent(String skillMdContent) { this.skillMdContent = skillMdContent; }
    public SkillVersionStatus getStatus() { return status; }
    public void setStatus(SkillVersionStatus status) { this.status = status; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public LocalDateTime getWithdrawTime() { return withdrawTime; }
    public void setWithdrawTime(LocalDateTime withdrawTime) { this.withdrawTime = withdrawTime; }
    public void setRepository(SkillVersionRepository repository) { this.repository = repository; }
    public void setGateway(SkillGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
