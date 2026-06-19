package com.agent.ops.domain.sandbox;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.sandbox.enums.SandboxStatus;
import com.agent.ops.domain.sandbox.event.SandboxEventConstant;
import com.agent.ops.domain.sandbox.gateway.SandboxGateway;
import com.agent.ops.domain.sandbox.repository.SandboxRepository;
import com.agent.ops.facade.domain.DomainEntity;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;

import java.time.LocalDateTime;

/**
 * 沙箱聚合根。
 * <p>
 * 状态机：
 * <ul>
 * <li>DRAFT → INITIALIZING (submit)</li>
 * <li>X (≠DRAFT) → DISABLED (disable)</li>
 * <li>DISABLED → INITIALIZING (reEnable)</li>
 * <li>{INITIALIZING, ONLINE, OFFLINE} → ONLINE (markOnline)</li>
 * <li>{INITIALIZING, ONLINE, OFFLINE} → OFFLINE (markOffline)</li>
 * <li>DRAFT → DELETED (delete)</li>
 * </ul>
 */
public class SandboxAggregate extends DomainEntity {
    private String spaceCode;
    private String name;
    private String image;
    private String baseUrlOverride;
    private String remark;
    private SandboxStatus status;
    private String lastStatusReason;
    private LocalDateTime lastHeartbeatTime;

    private SandboxRepository repository;
    private SandboxGateway gateway;
    private DomainEventPublisher eventPublisher;

    public SandboxAggregate() {
    }

    public SandboxAggregate(SandboxRepository repository, SandboxGateway gateway, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void save(String operatorCode) {
        assertCollaboratorsReady();
        boolean isNew = (getId() == null);
        if (status == null) {
            status = SandboxStatus.DRAFT;
        }
        if (StrUtil.isBlank(getNum())) {
            setNum(gateway.generateSandboxCode());
        }
        // 当 status ∉ {DRAFT, DISABLED} 时不允许修改 image
        // 由 domainValidate 在更新时校验（这里通过比对仓储现有值）
        initialize(operatorCode);
        validate();
        repository.save(this);
        if (isNew) {
            publishEvent(SandboxEventConstant.CREATED, operatorCode);
        }
    }

    public void submit(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SandboxStatus.DRAFT, "仅草稿态可提交");
        this.status = SandboxStatus.INITIALIZING;
        this.lastStatusReason = "用户提交";
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SandboxEventConstant.SUBMITTED, operatorCode);
    }

    public void disable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status != null && status != SandboxStatus.DRAFT, "草稿态不可禁用");
        this.status = SandboxStatus.DISABLED;
        this.lastStatusReason = "人工禁用";
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SandboxEventConstant.DISABLED, operatorCode);
    }

    public void reEnable(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SandboxStatus.DISABLED, "仅禁用态可重新启用");
        this.status = SandboxStatus.INITIALIZING;
        this.lastStatusReason = "人工启用";
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SandboxEventConstant.RE_ENABLED, operatorCode);
    }

    public void markOnline(String reason, String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SandboxStatus.INITIALIZING || status == SandboxStatus.ONLINE
                || status == SandboxStatus.OFFLINE, "当前状态不允许标在线");
        this.status = SandboxStatus.ONLINE;
        this.lastStatusReason = reason;
        this.lastHeartbeatTime = LocalDateTimeUtil.now();
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SandboxEventConstant.ONLINE, operatorCode);
    }

    public void markOffline(String reason, String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SandboxStatus.INITIALIZING || status == SandboxStatus.ONLINE
                || status == SandboxStatus.OFFLINE, "当前状态不允许标离线");
        this.status = SandboxStatus.OFFLINE;
        this.lastStatusReason = reason;
        this.lastHeartbeatTime = LocalDateTimeUtil.now();
        initialize(operatorCode);
        repository.save(this);
        publishEvent(SandboxEventConstant.OFFLINE, operatorCode);
    }

    @Override
    public void delete(String operatorCode) {
        assertCollaboratorsReady();
        Assert.isTrue(status == SandboxStatus.DRAFT, "仅草稿态可删除");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
        publishEvent(SandboxEventConstant.DELETED, operatorCode);
    }

    @Override
    public void domainValidate() {
        Assert.notBlank(spaceCode, "spaceCode 不能为空");
        Assert.notBlank(name, "name 不能为空");
        Assert.isTrue(StrUtil.length(name) <= 50, "name 长度不能超过 50 字符");
        Assert.notBlank(image, "image 不能为空");
        Assert.isTrue(StrUtil.length(image) <= 200, "image 长度不能超过 200 字符");
        Assert.notNull(status, "status 不能为空");
        // 唯一性
        Assert.notNull(repository, "repository 不能为空");
        if (repository.existsByName(spaceCode, name, getNum())) {
            throw new cn.hutool.core.exceptions.ValidateException("名称已存在");
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
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getBaseUrlOverride() { return baseUrlOverride; }
    public void setBaseUrlOverride(String baseUrlOverride) { this.baseUrlOverride = baseUrlOverride; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public SandboxStatus getStatus() { return status; }
    public void setStatus(SandboxStatus status) { this.status = status; }
    public String getLastStatusReason() { return lastStatusReason; }
    public void setLastStatusReason(String lastStatusReason) { this.lastStatusReason = lastStatusReason; }
    public LocalDateTime getLastHeartbeatTime() { return lastHeartbeatTime; }
    public void setLastHeartbeatTime(LocalDateTime lastHeartbeatTime) { this.lastHeartbeatTime = lastHeartbeatTime; }
    public void setRepository(SandboxRepository repository) { this.repository = repository; }
    public void setGateway(SandboxGateway gateway) { this.gateway = gateway; }
    public void setEventPublisher(DomainEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }
}
