package com.agent.ops.domain.prompt.gateway;

import java.util.List;

/**
 * Prompt 领域网关：仅承载本领域职能。
 */
public interface PromptGateway {
    /**
     * 生成 Prompt 业务编码。
     *
     * @return 业务编码
     */
    String generatePromptCode();

    /**
     * 解析 Prompt 内容中的 `{{varName}}` 占位符列表。
     *
     * @param content Prompt 内容
     * @return 变量名列表（去重）
     */
    List<String> extractVariables(String content);
}
