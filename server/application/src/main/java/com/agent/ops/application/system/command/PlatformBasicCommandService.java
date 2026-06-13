package com.agent.ops.application.system.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.system.dto.PlatformBasicDTO;
import com.agent.ops.client.system.enums.SystemSettingCategory;
import com.agent.ops.client.system.param.UpdatePlatformBasicParam;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.factory.SystemSettingFactory;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台基础信息写应用服务。
 */
@Service
public class PlatformBasicCommandService {
    /**
     * 工厂。
     */
    @Resource
    private SystemSettingFactory systemSettingFactory;

    /**
     * 加密器（用于密钥脱敏检测）。
     */
    @Resource
    private SecretEncryptor secretEncryptor;

    /**
     * Redis 锁。
     */
    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 更新平台基础信息。
     *
     * @param param 参数
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public PlatformBasicDTO update(UpdatePlatformBasicParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("system:platform_basic", () -> {
            String category = SystemSettingCategory.PLATFORM_BASIC.getCode();
            SystemSettingAggregate setting = systemSettingFactory.createByCategory(category);
            JSONObject json = setting == null ? new JSONObject() : JSONObject.parseObject(setting.getSettingJson());
            if (StrUtil.isNotBlank(param.platformName)) {
                json.put("platformName", param.platformName);
            }
            if (param.logoUrl != null) {
                json.put("logoUrl", param.logoUrl);
            }
            // 密钥：mask 占位或空字符串时不更新
            if (StrUtil.isNotBlank(param.encryptionKey) && !secretEncryptor.isEncrypted(param.encryptionKey)
                    && !param.encryptionKey.contains("****")) {
                json.put("encryptionKey", param.encryptionKey);
            }
            String mergedJson = json.toJSONString();
            if (setting == null) {
                setting = systemSettingFactory.create(category, mergedJson);
            } else {
                setting.setSettingJson(mergedJson);
            }
            setting.save(param.getOperatorCode());
            return toDTO(json);
        });
    }

    /**
     * JSON → DTO（脱敏密钥）。
     *
     * @param json JSON
     * @return DTO
     */
    private PlatformBasicDTO toDTO(JSONObject json) {
        PlatformBasicDTO dto = new PlatformBasicDTO();
        dto.platformName = json.getString("platformName");
        dto.logoUrl = json.getString("logoUrl");
        String key = json.getString("encryptionKey");
        dto.encryptionKey = StrUtil.isBlank(key) ? null : secretEncryptor.mask(key);
        return dto;
    }
}
