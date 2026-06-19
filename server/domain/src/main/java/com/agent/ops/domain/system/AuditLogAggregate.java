package com.agent.ops.domain.system;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.system.repository.AuditLogRepository;
import com.agent.ops.facade.domain.DomainEntity;

/**
 * 审计日志聚合根。
 */
public class AuditLogAggregate extends DomainEntity {
    /**
     * 模块名。
     */
    private String module;

    /**
     * 事件名。
     */
    private String action;

    /**
     * 操作人业务编码。
     */
    private String operatorCodeField;

    /**
     * 目标资源业务编码。
     */
    private String targetNum;

    /**
     * 明细 JSON。
     */
    private String detailJson;

    /**
     * 仓储。
     */
    private AuditLogRepository repository;

    /**
     * 默认构造。
     */
    public AuditLogAggregate() {
    }

    /**
     * 注入领域协作依赖。
     *
     * @param repository 仓储
     */
    public AuditLogAggregate(AuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * 持久化。
     *
     * @param operatorCode 当前操作人
     */
    @Override
    public void save(String operatorCode) {
        Assert.notNull(repository, "仓储不能为空");
        Assert.notBlank(module, "module 不能为空");
        Assert.notBlank(action, "action 不能为空");
        Assert.notBlank(operatorCodeField, "operatorCode 不能为空");
        if (StrUtil.isBlank(getNum())) {
            // 编码由应用层 Factory 提前生成（避免本聚合再依赖 Gateway）
            // 兜底：生成时本字段必须由调用方填好
            throw new IllegalStateException("AuditLog num must be generated before save");
        }
        initialize(operatorCode);
        validate();
        repository.save(this);
    }

    /**
     * 软删除（极少用）。
     *
     * @param operatorCode 当前操作人
     */
    @Override
    public void delete(String operatorCode) {
        Assert.notNull(repository, "仓储不能为空");
        Assert.notBlank(getNum(), "num 不能为空");
        initialize(operatorCode);
        repository.deleteByNum(getNum(), operatorCode);
    }

    /**
     * 校验业务不变量。
     */
    @Override
    public void domainValidate() {
        Assert.notBlank(module, "module 不能为空");
        Assert.notBlank(action, "action 不能为空");
        Assert.notBlank(operatorCodeField, "operatorCode 不能为空");
    }

    /**
     * 返回 module。
     *
     * @return module
     */
    public String getModule() {
        return module;
    }

    /**
     * 设置 module。
     *
     * @param module module
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * 返回 action。
     *
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * 设置 action。
     *
     * @param action action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * 返回操作人业务编码。
     *
     * @return operatorCode
     */
    public String getOperatorCodeField() {
        return operatorCodeField;
    }

    /**
     * 设置操作人业务编码。
     *
     * @param operatorCode 业务编码
     */
    public void setOperatorCodeField(String operatorCode) {
        this.operatorCodeField = operatorCode;
    }

    /**
     * 返回 targetNum。
     *
     * @return targetNum
     */
    public String getTargetNum() {
        return targetNum;
    }

    /**
     * 设置 targetNum。
     *
     * @param targetNum 目标资源业务编码
     */
    public void setTargetNum(String targetNum) {
        this.targetNum = targetNum;
    }

    /**
     * 返回 detailJson。
     *
     * @return detailJson
     */
    public String getDetailJson() {
        return detailJson;
    }

    /**
     * 设置 detailJson。
     *
     * @param detailJson 明细
     */
    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }

    /**
     * 设置 repository。
     *
     * @param repository 仓储
     */
    public void setRepository(AuditLogRepository repository) {
        this.repository = repository;
    }
}
