package com.agent.ops.facade.common.tool;

import java.util.Map;

/**
 * 工具试运行客户端契约。infra 层负责按子类型派发到具体实现。
 */
public interface ToolTestClient {
    /**
     * 试运行：FunctionCall 走真实 HTTP；MCP 走 list_tools。
     *
     * @param toolType         工具类型 FUNCTION_CALL / MCP
     * @param toolSubType      工具子类型 OPENAPI / ENDPOINT / REMOTE / LOCAL
     * @param decryptedConfig  已解密敏感字段的配置 JSON 字符串
     * @param testInput        试运行入参（path/query/header/body 等实参）
     * @return 试运行结果（含 success / durationMs / request / response / errorMessage 等键）
     */
    Map<String, Object> invokeTest(String toolType, String toolSubType, String decryptedConfig, Map<String, Object> testInput);
}
