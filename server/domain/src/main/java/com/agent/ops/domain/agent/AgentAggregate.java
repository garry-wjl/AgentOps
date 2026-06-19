package com.agent.ops.domain.agent;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.agent.enums.AgentStatus;
import com.agent.ops.domain.agent.event.AgentEventConstant;
import com.agent.ops.domain.agent.gateway.AgentGateway;
import com.agent.ops.domain.agent.repository.AgentRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 主体聚合根。三态：DRAFT / EFFECTIVE / WITHDRAWN。
 * <p>
 * name 仅允许英文字母/数字/下划线/中划线，必须以字母或下划线开头；保存后不可修改。
 */
public class AgentAggregate extends DomainEntity {
    private static final String NAME_REGEX = "^[A-Za-z_][A-Za-z0-9_-]{0,63}$";

    private String spaceCode;
    private String name;
    private String displayName;
    private String description;
    private String currentVersionNo;
    private AgentStatus status;
    private List<String> tags;
    private String remark;

    private AgentRepository repository;
    private AgentGateway gateway;
    private DomainEventPublisher eventPublisher;

    public AgentAggregate() { }

    public AgentAggregate(AgentRepository repository, AgentGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = AgentStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateAgentCode());
        }
        // name 不可修改：更新时强制使用旧值
        if (!isNew) {
            AgentAggregate existing = repository.findByNum(getNum());
            if (existing != null && !StrUtil.equals(existing.getName(), name)) {
                throw new cn.hutool.core.exceptions.ValidateException("Agent name 一经创建不可修改");
            }
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(AgentEventConstant.AGENT_CREATED, operatorCode);
        }
    }

    public void enable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == AgentStatus.DRAFT || status == AgentStatus.WITHDRAWN,
                "仅草稿或下架态可启用");
        this.status = AgentStatus.EFFECTIVE;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(AgentEventConstant.AGENT_ENABLED, operatorCode);
    }

    public void withdraw(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == AgentStatus.EFFECTIVE, "仅生效态可下架");
        this.status = AgentStatus.WITHDRAWN;
        initialize(operatorCode);
        repository.save(this);
        publishEvent(AgentEventConstant.AGENT_WITHDRAWN, operatorCode);
    }

    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == AgentStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(AgentEventConstant.AGENT_DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notBlank(name, "name 不能为空");
        Assert.isTrue(ReUtil.isMatch(NAME_REGEX, name),
                "name 仅允许英文字母/数字/下划线/中划线，且必须以字母或下划线开头");
        Assert.notNull(status, "status 不能为空");
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByName(spaceCode, name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("Agent name 已存在");
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
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCurrentVersionNo() { return currentVersionNo; }
    public void setCurrentVersionNo(String currentVersionNo) { this.currentVersionNo = currentVersionNo; }
    public AgentStatus getStatus() { return status; }
    public void setStatus(AgentStatus status) { this.status = status; }
    public List<String> getTags() { return tags == null ? new ArrayList<>() : tags; }
    public void setTags(List<String> tags) { this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags); }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setRepository(AgentRepository repository) { this.repository = repository; }
    public void setGateway(AgentGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
