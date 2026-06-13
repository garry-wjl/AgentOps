package com.agent.ops.application.system.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.system.dto.SandboxDefaultDTO;
import com.agent.ops.client.system.enums.SystemSettingCategory;
import com.agent.ops.client.system.param.UpdateSandboxDefaultParam;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.factory.SystemSettingFactory;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 沙箱默认写应用服务。
 */
@Service
public class SandboxDefaultCommandService {
    @Resource
    private SystemSettingFactory systemSettingFactory;

    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 更新沙箱默认。
     *
     * @param param 参数
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SandboxDefaultDTO update(UpdateSandboxDefaultParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("system:sandbox_default", () -> {
            String category = SystemSettingCategory.SANDBOX_DEFAULT.getCode();
            SystemSettingAggregate setting = systemSettingFactory.createByCategory(category);
            JSONObject json = setting == null ? new JSONObject() : JSONObject.parseObject(setting.getSettingJson());
            if (StrUtil.isNotBlank(param.baseUrl)) {
                json.put("baseUrl", param.baseUrl);
            }
            if (param.heartbeatIntervalSec != null) {
                json.put("heartbeatIntervalSec", param.heartbeatIntervalSec);
            }
            String mergedJson = json.toJSONString();
            if (setting == null) {
                setting = systemSettingFactory.create(category, mergedJson);
            } else {
                setting.setSettingJson(mergedJson);
            }
            setting.save(param.getOperatorCode());
            SandboxDefaultDTO dto = new SandboxDefaultDTO();
            dto.baseUrl = json.getString("baseUrl");
            dto.heartbeatIntervalSec = json.getInteger("heartbeatIntervalSec");
            return dto;
        });
    }
}
