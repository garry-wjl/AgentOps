package com.agent.ops.infra.tool.gateway;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.tool.enums.ToolSubType;
import com.agent.ops.client.tool.enums.ToolType;
import com.agent.ops.domain.tool.gateway.ToolGateway;
import com.agent.ops.facade.common.code.BizCodeGenerator;
import com.agent.ops.facade.common.code.BizCodePrefix;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 工具网关实现：编码生成 + configJson 校验 + 敏感字段加密。
 */
@Component
public class ToolGatewayImpl implements ToolGateway {
    /**
     * 敏感字段名关键字（命中则按敏感存储）。
     */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "value", "passwordCipher", "password", "apiKey", "token", "secret",
            "authorization", "Authorization");

    /**
     * 系统保留请求头名（不允许由用户覆盖）。
     */
    private static final Set<String> RESERVED_HEADERS = Set.of(
            "Host", "Content-Length", "Connection", "Transfer-Encoding",
            "Upgrade", "Proxy-Authorization");

    @Resource
    private BizCodeGenerator bizCodeGenerator;

    @Resource
    private SecretEncryptor secretEncryptor;

    @Override
    public String generateToolCode() {
        return bizCodeGenerator.generate(BizCodePrefix.TOOL);
    }

    @Override
    public void validateConfig(ToolType type, ToolSubType subType, String json) {
        Assert.notNull(type, "type 不能为空");
        Assert.notNull(subType, "subType 不能为空");
        Assert.notBlank(json, "configJson 不能为空");
        JSONObject obj = JSONObject.parseObject(json);
        switch (subType) {
            case OPENAPI -> validateOpenApi(obj);
            case ENDPOINT -> validateEndpoint(obj);
            case REMOTE -> validateRemoteMcp(obj);
            case LOCAL -> validateLocalMcp(obj);
            default -> throw new cn.hutool.core.exceptions.ValidateException("未知的工具子类型: " + subType);
        }
        validateHeadersIfPresent(obj.getJSONArray("headers"));
    }

    private void validateOpenApi(JSONObject obj) {
        JSONObject spec = obj.getJSONObject("spec");
        Assert.notNull(spec, "spec 不能为空");
        Assert.notBlank(spec.getString("content"), "spec.content 不能为空");
        Assert.notBlank(obj.getString("baseUrl"), "baseUrl 不能为空");
        Assert.isTrue(obj.getString("baseUrl").startsWith("http://") || obj.getString("baseUrl").startsWith("https://"),
                "baseUrl 必须以 http(s):// 开头");
        JSONArray ops = obj.getJSONArray("selectedOperations");
        Assert.notNull(ops, "selectedOperations 不能为空");
        Assert.isTrue(!ops.isEmpty(), "至少选择 1 个 Operation");
    }

    private void validateEndpoint(JSONObject obj) {
        JSONObject endpoint = obj.getJSONObject("endpoint");
        Assert.notNull(endpoint, "endpoint 不能为空");
        Assert.notBlank(endpoint.getString("method"), "method 不能为空");
        Assert.notBlank(endpoint.getString("url"), "url 不能为空");
        Assert.isTrue(endpoint.getString("url").startsWith("http://") || endpoint.getString("url").startsWith("https://"),
                "url 必须以 http(s):// 开头");
        String method = endpoint.getString("method").toUpperCase();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            JSONObject body = endpoint.getJSONObject("body");
            if (body != null) {
                String contentType = body.getString("contentType");
                if (StrUtil.isNotBlank(contentType) && !"none".equals(contentType)) {
                    throw new cn.hutool.core.exceptions.ValidateException(method + " 方法不允许携带 body");
                }
            }
        }
    }

    private void validateRemoteMcp(JSONObject obj) {
        Assert.notBlank(obj.getString("transport"), "transport 不能为空");
        Assert.notBlank(obj.getString("url"), "url 不能为空");
        Assert.isTrue(obj.getString("url").startsWith("http://") || obj.getString("url").startsWith("https://"),
                "MCP URL 必须以 http(s):// 开头");
    }

    private void validateLocalMcp(JSONObject obj) {
        Assert.notBlank(obj.getString("command"), "command 不能为空");
        String cmd = obj.getString("command");
        for (String forbidden : new String[]{";", "&&", "||", "|", "`", "$("}) {
            if (cmd.contains(forbidden)) {
                throw new cn.hutool.core.exceptions.ValidateException("command 包含禁止字符: " + forbidden);
            }
        }
        JSONArray env = obj.getJSONArray("env");
        if (env != null) {
            for (Object item : env) {
                if (item instanceof JSONObject envObj) {
                    String name = envObj.getString("name");
                    Assert.notBlank(name, "env.name 不能为空");
                    Assert.isTrue(name.matches("^[A-Z_][A-Z0-9_]*$"),
                            "env.name 必须匹配 ^[A-Z_][A-Z0-9_]*$ : " + name);
                }
            }
        }
    }

    private void validateHeadersIfPresent(JSONArray headers) {
        if (headers == null) {
            return;
        }
        for (Object item : headers) {
            if (item instanceof JSONObject h) {
                String name = h.getString("name");
                if (RESERVED_HEADERS.contains(name)) {
                    throw new cn.hutool.core.exceptions.ValidateException("禁止覆盖系统保留请求头: " + name);
                }
            }
        }
    }

    @Override
    public String encryptSensitiveFields(String json) {
        if (StrUtil.isBlank(json)) {
            return json;
        }
        Object parsed = JSON.parse(json);
        encryptInPlace(parsed);
        return JSON.toJSONString(parsed);
    }

    private void encryptInPlace(Object node) {
        if (node instanceof JSONObject obj) {
            for (String key : new java.util.HashSet<>(obj.keySet())) {
                Object value = obj.get(key);
                if (value instanceof String str && SENSITIVE_KEYS.contains(key)
                        && StrUtil.isNotBlank(str) && !secretEncryptor.isEncrypted(str)) {
                    obj.put(key, secretEncryptor.encrypt(str));
                } else if (value instanceof JSONObject || value instanceof JSONArray) {
                    encryptInPlace(value);
                }
            }
        } else if (node instanceof JSONArray arr) {
            for (Object item : arr) {
                encryptInPlace(item);
            }
        }
    }
}
