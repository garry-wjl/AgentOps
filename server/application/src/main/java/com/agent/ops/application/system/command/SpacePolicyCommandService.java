package com.agent.ops.application.system.command;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.agent.ops.client.system.dto.SpacePolicyDTO;
import com.agent.ops.client.system.enums.SystemSettingCategory;
import com.agent.ops.client.system.param.UpdateSpacePolicyParam;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.factory.SystemSettingFactory;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 空间策略写应用服务。
 */
@Service
public class SpacePolicyCommandService {
    @Resource
    private SystemSettingFactory systemSettingFactory;

    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 更新空间策略。
     *
     * @param param 参数
     * @return DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SpacePolicyDTO update(UpdateSpacePolicyParam param) {
        Assert.notNull(param, "参数不能为空");
        Assert.notBlank(param.getOperatorCode(), "operatorCode 不能为空");
        return distributedLock.execute("system:space_policy", () -> {
            String category = SystemSettingCategory.SPACE_POLICY.getCode();
            SystemSettingAggregate setting = systemSettingFactory.createByCategory(category);
            JSONObject json = setting == null ? new JSONObject() : JSONObject.parseObject(setting.getSettingJson());
            if (param.quotaPerUser != null) {
                json.put("quotaPerUser", param.quotaPerUser);
            }
            if (StrUtil.isNotBlank(param.namingRegex)) {
                json.put("namingRegex", param.namingRegex);
            }
            String mergedJson = json.toJSONString();
            if (setting == null) {
                setting = systemSettingFactory.create(category, mergedJson);
            } else {
                setting.setSettingJson(mergedJson);
            }
            setting.save(param.getOperatorCode());
            SpacePolicyDTO dto = new SpacePolicyDTO();
            dto.quotaPerUser = json.getInteger("quotaPerUser");
            dto.namingRegex = json.getString("namingRegex");
            return dto;
        });
    }
}
