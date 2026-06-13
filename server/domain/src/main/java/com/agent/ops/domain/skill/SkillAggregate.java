package com.agent.ops.domain.skill;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.skill.enums.SkillStatus;
import com.agent.ops.domain.skill.event.SkillEventConstant;
import com.agent.ops.domain.skill.gateway.SkillGateway;
import com.agent.ops.domain.skill.repository.SkillRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Skill 主体聚合根。三态：DRAFT / EFFECTIVE / WITHDRAWN。
 */
public class SkillAggregate extends DomainEntity {
    private String spaceCode;
    private String name;
    private String description;
    private String currentVersionNo;
    private SkillStatus status;
    private List<String> tags;
    private String remark;

    private SkillRepository repository;
    private SkillGateway gateway;
    private DomainEventPublisher eventPublisher;

    public SkillAggregate() { }

    public SkillAggregate(SkillRepository repository, SkillGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = SkillStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateSkillCode());
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(SkillEventConstant.SKILL_CREATED, operatorCode);
        }
    }

    public void enable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SkillStatus.DRAFT || status == SkillStatus.WITHDRAWN,
                "仅草稿或下架态可启用");
        this.status = SkillStatus.EFFECTIVE;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SkillEventConstant.SKILL_ENABLED, operatorCode);
    }

    public void withdraw(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SkillStatus.EFFECTIVE, "仅生效态可下架");
        this.status = SkillStatus.WITHDRAWN;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SkillEventConstant.SKILL_WITHDRAWN, operatorCode);
    }

    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SkillStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(SkillEventConstant.SKILL_DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notBlank(name, "name 不能为空");
        Assert.isTrue(StrUtil.length(name) <= 50, "name 长度不能超过 50 字符");
        Assert.notBlank(description, "description 不能为空");
        Assert.isTrue(StrUtil.length(description) <= 500, "description 长度不能超过 500 字符");
        Assert.notNull(status, "status 不能为空");
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByName(spaceCode, name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("Skill 名称已存在");
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

    public String getSpaceCode() { return spaceCode; }
    public void setSpaceCode(String spaceCode) { this.spaceCode = spaceCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCurrentVersionNo() { return currentVersionNo; }
    public void setCurrentVersionNo(String currentVersionNo) { this.currentVersionNo = currentVersionNo; }
    public SkillStatus getStatus() { return status; }
    public void setStatus(SkillStatus status) { this.status = status; }
    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }
    public void setTags(List<String> tags) { this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags); }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setRepository(SkillRepository repository) { this.repository = repository; }
    public void setGateway(SkillGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
