package com.agent.ops.infra.system.factory;

import cn.hutool.core.lang.Assert;
import com.agent.ops.domain.system.AuditLogAggregate;
import com.agent.ops.domain.system.factory.AuditLogFactory;
import com.agent.ops.domain.system.gateway.SystemSettingGateway;
import com.agent.ops.domain.system.repository.AuditLogRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 审计日志工厂实现。
 */
@Component
public class AuditLogFactoryImpl implements AuditLogFactory {
    /**
     * 仓储。
     */
    @Resource
    private AuditLogRepository auditLogRepository;

    /**
     * 网关（编码生成）。
     */
    @Resource
    private SystemSettingGateway systemSettingGateway;

    /**
     * 创建新审计日志。
     *
     * @param module       模块
     * @param action       事件
     * @param operatorCode 操作人
     * @param targetNum    目标资源
     * @param detailJson   明细
     * @return 聚合
     */
    @Override
    public AuditLogAggregate create(String module, String action, String operatorCode, String targetNum, String detailJson) {
        Assert.notBlank(module, "module 不能为空");
        Assert.notBlank(action, "action 不能为空");
        Assert.notBlank(operatorCode, "operatorCode 不能为空");
        AuditLogAggregate a = new AuditLogAggregate(auditLogRepository);
        a.setNum(systemSettingGateway.generateAuditLogCode());
        a.setModule(module);
        a.setAction(action);
        a.setOperatorCodeField(operatorCode);
        a.setTargetNum(targetNum);
        a.setDetailJson(detailJson);
        return a;
    }

    /**
     * 按业务编码加载。
     *
     * @param num 业务编码
     * @return 聚合
     */
    @Override
    public AuditLogAggregate createByNum(String num) {
        Assert.notBlank(num, "num 不能为空");
        AuditLogAggregate a = auditLogRepository.findByNum(num);
        if (a == null) {
            return null;
        }
        a.setRepository(auditLogRepository);
        return a;
    }
}
