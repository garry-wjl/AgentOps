package com.agent.ops.domain.agent;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.agent.enums.AgentVersionStatus;
import com.agent.ops.domain.agent.event.AgentEventConstant;
import com.agent.ops.domain.agent.gateway.AgentGateway;
import com.agent.ops.domain.agent.repository.AgentVersionRepository;
import com.agent.ops.domain.agent.valueobject.AssemblySnapshot;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Agent 版本聚合根。
 */
public class AgentVersionAggregate extends DomainEntity {
    private String agentCode;
    private String versionNo;
    private AssemblySnapshot snapshot;
    private AgentVersionStatus status;
    private LocalDateTime onlineTime;
    private LocalDateTime offlineTime;

    private AgentVersionRepository repository;
    private AgentGateway gateway;
    private DomainEventPublisher eventPublisher;

    public AgentVersionAggregate() { }

    public AgentVersionAggregate(AgentVersionRepository repository, AgentGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = AgentVersionStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateVersionCode());
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(AgentEventConstant.VERSION_CREATED, operatorCode);
        }
    }

    public void publish(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == AgentVersionStatus.DRAFT, "仅草稿态可发布");
        // 顶替原 ONLINE 版本
        AgentVersionAggregate old = repository.findOnlineByAgentCode(agentCode);
        if (old != null && !StrUtil.equals(old.getNum(), getNum())) {
            old.setRepository(repository);
            old.setGateway(gateway);
            old.setEventPublisher(eventPublisher);
            old.offlineInternal(operatorCode);
        }
        this.status = AgentVersionStatus.ONLINE;
        this.onlineTime = LocalDateTimeUtil.now();
        initialize(operatorCode);
        repository.save(this);
        publishEvent(AgentEventConstant.VERSION_PUBLISHED, operatorCode);
    }

    public void offline(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == AgentVersionStatus.ONLINE, "仅在线态可下线");
        offlineInternal(operatorCode);
    }

    private void offlineInternal(String operatorCode) {
        this.status = AgentVersionStatus.OFFLINE;
        this.offlineTime = LocalDateTimeUtil.now();
        initialize(operatorCode);
        repository.save(this);
        publishEvent(AgentEventConstant.VERSION_OFFLINED, operatorCode);
    }

    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == AgentVersionStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(AgentEventConstant.VERSION_DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(agentCode, "agentCode 不能为空");
        Assert.notBlank(versionNo, "versionNo 不能为空");
        Assert.notNull(status, "status 不能为空");
        Assert.notNull(snapshot, "snapshot 不能为空");
        Assert.notBlank(snapshot.getSystemPromptContent(), "systemPromptContent 不能为空");
        if (snapshot.getSkillCodes() != null) {
            Assert.isTrue(snapshot.getSkillCodes().size() <= 50, "skill 数量不能超过 50");
            Assert.isTrue(new HashSet<>(snapshot.getSkillCodes()).size() == snapshot.getSkillCodes().size(),
                    "skillCodes 不允许重复");
        }
        if (snapshot.getToolCodes() != null) {
            Assert.isTrue(snapshot.getToolCodes().size() <= 50, "tool 数量不能超过 50");
            Assert.isTrue(new HashSet<>(snapshot.getToolCodes()).size() == snapshot.getToolCodes().size(),
                    "toolCodes 不允许重复");
        }
        if (snapshot.getShortMemoryTurns() != null) {
            Assert.isTrue(snapshot.getShortMemoryTurns() >= 0 && snapshot.getShortMemoryTurns() <= 50,
                    "shortMemoryTurns 必须在 0~50 之间");
        }
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByVersionNo(agentCode, versionNo, getNum())) {
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

    public String getAgentCode() { return agentCode; }
    public void setAgentCode(String agentCode) { this.agentCode = agentCode; }
    public String getVersionNo() { return versionNo; }
    public void setVersionNo(String versionNo) { this.versionNo = versionNo; }
    public AssemblySnapshot getSnapshot() { return snapshot; }
    public void setSnapshot(AssemblySnapshot snapshot) { this.snapshot = snapshot; }
    public AgentVersionStatus getStatus() { return status; }
    public void setStatus(AgentVersionStatus status) { this.status = status; }
    public LocalDateTime getOnlineTime() { return onlineTime; }
    public void setOnlineTime(LocalDateTime onlineTime) { this.onlineTime = onlineTime; }
    public LocalDateTime getOfflineTime() { return offlineTime; }
    public void setOfflineTime(LocalDateTime offlineTime) { this.offlineTime = offlineTime; }
    public void setRepository(AgentVersionRepository repository) { this.repository = repository; }
    public void setGateway(AgentGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
