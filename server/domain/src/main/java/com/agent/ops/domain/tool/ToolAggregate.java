package com.agent.ops.domain.tool;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.domain.tool.event.ToolEventConstant;
import com.agent.ops.domain.tool.gateway.ToolGateway;
import com.agent.ops.domain.tool.repository.ToolRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具聚合根。
 */
public class ToolAggregate extends DomainEntity {
    private String spaceCode;
    private String name;
    private ToolType type;
    private ToolSubType subType;
    private String description;
    private List<String> tags;
    private String configJson;
    private ToolStatus status;
    private String remark;

    private ToolRepository repository;
    private ToolGateway gateway;
    private DomainEventPublisher eventPublisher;

    public ToolAggregate() {
    }

    public ToolAggregate(ToolRepository repository, ToolGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = ToolStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateToolCode());
        }
        // 配置 JSON 校验 + 敏感字段加密
        gateway.validateConfig(type, subType, configJson);
        this.configJson = gateway.encryptSensitiveFields(configJson);
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(ToolEventConstant.CREATED, operatorCode);
        }
    }

    public void publish(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ToolStatus.DRAFT, "仅草稿态可发布");
        gateway.validateConfig(type, subType, configJson);
        this.status = ToolStatus.EFFECTIVE;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(ToolEventConstant.PUBLISHED, operatorCode);
    }

    public void withdraw(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ToolStatus.EFFECTIVE, "仅生效态可下架");
        this.status = ToolStatus.WITHDRAWN;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(ToolEventConstant.WITHDRAWN, operatorCode);
    }

    public void republish(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ToolStatus.WITHDRAWN, "仅下架态可重新发布");
        this.status = ToolStatus.EFFECTIVE;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(ToolEventConstant.REPUBLISHED, operatorCode);
    }

    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == ToolStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(ToolEventConstant.DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notBlank(name, "name 不能为空");
        Assert.isTrue(StrUtil.length(name) <= 50, "name 长度不能超过 50 字符");
        Assert.notNull(type, "type 不能为空");
        Assert.notNull(subType, "subType 不能为空");
        Assert.notBlank(configJson, "configJson 不能为空");
        Assert.notNull(status, "status 不能为空");
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByName(spaceCode, name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("工具名称已存在");
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
    public ToolType getType() { return type; }
    public void setType(ToolType type) { this.type = type; }
    public ToolSubType getSubType() { return subType; }
    public void setSubType(ToolSubType subType) { this.subType = subType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }
    public void setTags(List<String> tags) { this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags); }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public ToolStatus getStatus() { return status; }
    public void setStatus(ToolStatus status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setRepository(ToolRepository repository) { this.repository = repository; }
    public void setGateway(ToolGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
