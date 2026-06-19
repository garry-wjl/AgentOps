package com.agent.ops.infra.system.gateway;

import com.agent.ops.domain.system.gateway.SystemSettingGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 系统设置网关实现。
 */
@Component
public class SystemSettingGatewayImpl implements SystemSettingGateway {
    /**
     * 业务编码生成器。
     */
    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Override
    public String generateSettingCode() {
        return bizCodeGenerator.generate(BizCodePrefix.SYSTEM_SETTING);
    }

    @Override
    public String generateAuditLogCode() {
        return bizCodeGenerator.generate(BizCodePrefix.AUDIT_LOG);
    }
}
