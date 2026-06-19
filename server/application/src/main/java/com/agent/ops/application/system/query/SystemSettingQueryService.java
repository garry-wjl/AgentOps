package com.agent.ops.application.system.query;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.system.dto.PlatformBasicDTO;
import com.agent.ops.client.system.dto.SandboxDefaultDTO;
import com.agent.ops.client.system.dto.SmtpConfigDTO;
import com.agent.ops.client.system.dto.SpacePolicyDTO;
import com.agent.ops.client.system.enums.SystemSettingCategory;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.repository.SystemSettingRepository;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 系统设置读应用服务。
 */
@Service
public class SystemSettingQueryService {
    @Resource
    private SystemSettingRepository systemSettingRepository;

    @Resource
    private SecretEncryptor secretEncryptor;

    /**
     * 平台基本信息（密钥脱敏）。
     *
     * @return DTO
     */
    public PlatformBasicDTO getPlatformBasic() {
        JSONObject json = loadJson(SystemSettingCategory.PLATFORM_BASIC.getCode());
        PlatformBasicDTO dto = new PlatformBasicDTO();
        dto.platformName = json.getString("platformName");
        dto.logoUrl = json.getString("logoUrl");
        String key = json.getString("encryptionKey");
        dto.encryptionKey = StrUtil.isBlank(key) ? null : secretEncryptor.mask(key);
        return dto;
    }

    /**
     * SMTP（密码脱敏）。
     *
     * @return DTO
     */
    public SmtpConfigDTO getSmtp() {
        JSONObject json = loadJson(SystemSettingCategory.SMTP.getCode());
        SmtpConfigDTO dto = new SmtpConfigDTO();
        dto.host = json.getString("host");
        dto.port = json.getInteger("port");
        dto.username = json.getString("username");
        String pwdCipher = json.getString("passwordCipher");
        dto.password = StrUtil.isBlank(pwdCipher) ? null : secretEncryptor.mask(pwdCipher);
        dto.from = json.getString("from");
        dto.ssl = json.getBoolean("ssl");
        return dto;
    }

    /**
     * 空间策略。
     *
     * @return DTO
     */
    public SpacePolicyDTO getSpacePolicy() {
        JSONObject json = loadJson(SystemSettingCategory.SPACE_POLICY.getCode());
        SpacePolicyDTO dto = new SpacePolicyDTO();
        dto.quotaPerUser = json.getInteger("quotaPerUser");
        dto.namingRegex = json.getString("namingRegex");
        return dto;
    }

    /**
     * 沙箱默认。
     *
     * @return DTO
     */
    public SandboxDefaultDTO getSandboxDefault() {
        JSONObject json = loadJson(SystemSettingCategory.SANDBOX_DEFAULT.getCode());
        SandboxDefaultDTO dto = new SandboxDefaultDTO();
        dto.baseUrl = json.getString("baseUrl");
        dto.heartbeatIntervalSec = json.getInteger("heartbeatIntervalSec");
        return dto;
    }

    /**
     * 加载分类 JSON。
     *
     * @param category 分类
     * @return JSON 对象（不存在返回空对象）
     */
    private JSONObject loadJson(String category) {
        SystemSettingAggregate aggregate = systemSettingRepository.findByCategory(category);
        if (aggregate == null || StrUtil.isBlank(aggregate.getSettingJson())) {
            return new JSONObject();
        }
        return JSONObject.parseObject(aggregate.getSettingJson());
    }
}
