package com.agent.ops.infra.tool.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.agent.ops.facade.common.tool.ToolTestClient;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具试运行客户端实现。
 * <p>
 * - FUNCTION_CALL/ENDPOINT：走真实 HTTP（Hutool）
 * - FUNCTION_CALL/OPENAPI：占位（取 baseUrl + 第一个 selectedOperation 拼 URL）
 * - MCP/REMOTE：占位（仅返回标记）
 * - MCP/LOCAL：占位（仅返回标记）
 * <p>
 * 占位部分待后续接入 SwaggerParser / mcp-java-sdk / ProcessBuilder 完整实现。
 */
@Component
public class ToolTestClientImpl implements ToolTestClient {
    private static final Logger log = LoggerFactory.getLogger(ToolTestClientImpl.class);

    @Override
    public Map<String, Object> invokeTest(String toolType, String toolSubType, String decryptedConfig, Map<String, Object> testInput) {
        long started = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            JSONObject config = JSONObject.parseObject(decryptedConfig);
            if ("FUNCTION_CALL".equals(toolType) && "ENDPOINT".equals(toolSubType)) {
                return doFunctionCallEndpoint(config, testInput, started);
            }
            if ("FUNCTION_CALL".equals(toolType) && "OPENAPI".equals(toolSubType)) {
                return doFunctionCallOpenApi(config, testInput, started);
            }
            // MCP 占位
            result.put("success", false);
            result.put("durationMs", System.currentTimeMillis() - started);
            result.put("errorMessage", "MCP 试运行尚未实现完整链路（type=" + toolType + ", subType=" + toolSubType + "）");
            return result;
        } catch (Exception e) {
            log.warn("[ToolTestClient] invokeTest failed: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("durationMs", System.currentTimeMillis() - started);
            result.put("errorMessage", e.getMessage());
            return result;
        }
    }

    private Map<String, Object> doFunctionCallEndpoint(JSONObject config, Map<String, Object> testInput, long started) {
        JSONObject endpoint = config.getJSONObject("endpoint");
        String method = endpoint.getString("method");
        String url = endpoint.getString("url");
        if (testInput != null && testInput.containsKey("pathParams")) {
            Object obj = testInput.get("pathParams");
            if (obj instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    url = url.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }
            }
        }
        HttpRequest request = HttpRequest.of(url).method(Method.valueOf(method)).timeout(10000);
        try (HttpResponse response = request.execute()) {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> req = new LinkedHashMap<>();
            req.put("method", method);
            req.put("url", url);
            result.put("request", req);
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", response.getStatus());
            resp.put("body", response.body());
            result.put("response", resp);
            result.put("success", response.isOk());
            result.put("durationMs", System.currentTimeMillis() - started);
            return result;
        }
    }

    private Map<String, Object> doFunctionCallOpenApi(JSONObject config, Map<String, Object> testInput, long started) {
        // 占位：仅返回 baseUrl + 第一个 operation
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("durationMs", System.currentTimeMillis() - started);
        result.put("errorMessage", "OpenAPI 试运行尚未实现完整 SwaggerParser 链路；baseUrl="
                + config.getString("baseUrl") + ", input=" + (testInput == null ? "{}" : testInput.toString()));
        if (StrUtil.isBlank(result.get("errorMessage").toString())) {
            result.put("errorMessage", "OpenAPI 试运行占位");
        }
        return result;
    }
}
