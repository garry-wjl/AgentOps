package com.agent.ops.infra.model.gateway;

import com.agent.ops.domain.model.gateway.ModelGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 模型网关实现。
 */
@Component
public class ModelGatewayImpl implements ModelGateway {
    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Resource
    private SecretEncryptor secretEncryptor;

    @Override
    public String generateModelCode() {
        return bizCodeGenerator.generate(BizCodePrefix.MODEL);
    }

    @Override
    public String encrypt(String plaintext) {
        return secretEncryptor.encrypt(plaintext);
    }
}
