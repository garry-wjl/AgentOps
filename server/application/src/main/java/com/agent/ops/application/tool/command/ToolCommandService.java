package com.agent.ops.application.tool.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.tool.dto.TestResultDTO;
import com.agent.ops.client.tool.dto.ToolDTO;
import com.agent.ops.client.tool.enums.ToolStatus;
import com.agent.ops.client.tool.param.CreateToolParam;
import com.agent.ops.client.tool.param.ToolActionParam;
import com.agent.ops.client.tool.param.ToolTestParam;
import com.agent.ops.client.tool.param.UpdateToolParam;
import com.agent.ops.domain.tool.ToolAggregate;
import com.agent.ops.domain.tool.factory.ToolFactory;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.facade.common.tool.ToolTestClient;
import com.agent.ops.facade.exception.BusinessException;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ToolCommandService {
    /**
     * 敏感字段名关键字。
     */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "value", "passwordCipher", "password", "apiKey", "token", "secret",
            "authorization", "Authorization");

    @Resource
    private ToolFactory toolFactory;

    @Resource
    private SecretEncryptor secretEncryptor;

    @Resource
    private ToolTestClient toolTestClient;

    @Resource
    private RedisDistributedLock distributedLock;

    @Transactional(rollbackFor = Exception.class)
    public ToolDTO create(CreateToolParam param) {
        Assert.notNull(param, "参数不能为空");
        return distributedLock.execute("tool:create:" + param.spaceCode + ":" + param.name, () -> {
            ToolAggregate a = toolFactory.create(param.spaceCode, param.name, param.type, param.subType,
                    param.description, param.tags, param.configJson, param.remark);
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public ToolDTO update(UpdateToolParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        return distributedLock.execute("tool:" + param.num, () -> {
            ToolAggregate a = loadAggregate(param.num);
            if (StrUtil.isNotBlank(param.name)) a.setName(param.name);
            if (param.description != null) a.setDescription(param.description);
            if (param.tags != null) a.setTags(param.tags);
            if (StrUtil.isNotBlank(param.configJson)) a.setConfigJson(param.configJson);
            if (param.remark != null) a.setRemark(param.remark);
            a.save(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public ToolDTO publish(ToolActionParam param) {
        return distributedLock.execute("tool:" + param.num, () -> {
            ToolAggregate a = loadAggregate(param.num);
            a.publish(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public ToolDTO withdraw(ToolActionParam param) {
        return distributedLock.execute("tool:" + param.num, () -> {
            ToolAggregate a = loadAggregate(param.num);
            a.withdraw(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public ToolDTO republish(ToolActionParam param) {
        return distributedLock.execute("tool:" + param.num, () -> {
            ToolAggregate a = loadAggregate(param.num);
            a.republish(param.getOperatorCode());
            return toDTO(a);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(ToolActionParam param) {
        distributedLock.run("tool:" + param.num, () -> {
            ToolAggregate a = loadAggregate(param.num);
            a.delete(param.getOperatorCode());
        });
    }

    /**
     * 试运行（不加锁，不改状态）。
     *
     * @param param 入参
     * @return 试运行结果
     */
    public TestResultDTO test(ToolTestParam param) {
        Assert.notBlank(param.num, "num 不能为空");
        ToolAggregate a = loadAggregate(param.num);
        // 解密敏感字段后传给 TestClient
        String decrypted = decryptSensitiveFields(a.getConfigJson());
        Map<String, Object> raw = toolTestClient.invokeTest(
                a.getType() == null ? null : a.getType().name(),
                a.getSubType() == null ? null : a.getSubType().name(),
                decrypted,
                param.testInput
        );
        TestResultDTO dto = new TestResultDTO();
        Object successObj = raw.get("success");
        dto.success = successObj instanceof Boolean ? (Boolean) successObj : Boolean.FALSE;
        Object durationObj = raw.get("durationMs");
        dto.durationMs = durationObj instanceof Number ? ((Number) durationObj).longValue() : null;
        Object requestObj = raw.get("request");
        if (requestObj instanceof Map<?, ?> reqMap) {
            //noinspection unchecked
            dto.request = (Map<String, Object>) reqMap;
        }
        Object responseObj = raw.get("response");
        if (responseObj instanceof Map<?, ?> respMap) {
            //noinspection unchecked
            dto.response = (Map<String, Object>) respMap;
        }
        dto.errorMessage = raw.get("errorMessage") == null ? null : raw.get("errorMessage").toString();
        return dto;
    }

    /**
     * 解密 JSON 中的敏感字段（递归）。
     *
     * @param json 含密文的 JSON
     * @return 已解密的 JSON
     */
    private String decryptSensitiveFields(String json) {
        if (StrUtil.isBlank(json)) {
            return json;
        }
        Object parsed = JSON.parse(json);
        decryptInPlace(parsed);
        return JSON.toJSONString(parsed);
    }

    private void decryptInPlace(Object node) {
        if (node instanceof JSONObject obj) {
            for (String key : new HashSet<>(obj.keySet())) {
                Object value = obj.get(key);
                if (value instanceof String str && SENSITIVE_KEYS.contains(key)
                        && secretEncryptor.isEncrypted(str)) {
                    obj.put(key, secretEncryptor.decrypt(str));
                } else if (value instanceof JSONObject || value instanceof JSONArray) {
                    decryptInPlace(value);
                }
            }
        } else if (node instanceof JSONArray arr) {
            for (Object item : arr) {
                decryptInPlace(item);
            }
        }
    }

    private ToolAggregate loadAggregate(String num) {
        ToolAggregate a = toolFactory.createByNum(num);
        if (a == null) {
            throw new BusinessException("TOOL_NOT_FOUND", "工具不存在");
        }
        return a;
    }

    private ToolDTO toDTO(ToolAggregate a) {
        ToolDTO dto = new ToolDTO();
        dto.num = a.getNum();
        dto.spaceCode = a.getSpaceCode();
        dto.name = a.getName();
        dto.type = a.getType();
        dto.subType = a.getSubType();
        dto.description = a.getDescription();
        dto.tags = a.getTags();
        // 配置 JSON 中的敏感字段不解密展示，原样返回（已是密文+脱敏由前端处理）
        dto.configJson = a.getConfigJson();
        dto.status = a.getStatus() == null ? ToolStatus.DRAFT : a.getStatus();
        dto.remark = a.getRemark();
        dto.createTime = a.getCreateTime();
        dto.updateTime = a.getUpdateTime();
        return dto;
    }
}
