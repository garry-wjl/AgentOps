package com.agent.ops.infra.system.config;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.system.SystemSettingAggregate;
import com.agent.ops.domain.system.event.SystemEventConstant;
import com.agent.ops.domain.system.repository.SystemSettingRepository;
import com.agent.ops.facade.common.crypto.SecretEncryptor;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 系统设置启动加载器。
 * <ul>
 * <li>启动时从 system_settings 加载 platform_basic.encryptionKey，刷新到 SecretEncryptor</li>
 * <li>订阅 system.setting.changed 事件，当 platform_basic 分类变更时再次刷新</li>
 * </ul>
 */
@Component
public class SystemSettingsLoader {
    private static final Logger log = LoggerFactory.getLogger(SystemSettingsLoader.class);

    /**
     * 系统设置仓储。
     */
    @Resource
    private SystemSettingRepository systemSettingRepository;

    /**
     * 加密器。
     */
    @Resource
    private SecretEncryptor secretEncryptor;

    /**
     * 应用启动时加载密钥。
     */
    @PostConstruct
    public void loadOnStartup() {
        try {
            refreshFromCategory("platform_basic");
        } catch (Exception e) {
            log.warn("[SystemSettingsLoader] 启动时未能加载 platform_basic.encryptionKey: {}", e.getMessage());
        }
    }

    /**
     * 监听系统设置变更事件，刷新对应缓存。
     *
     * @param event 领域事件
     */
    @EventListener
    public void onSettingChanged(DomainEventDTO event) {
        if (event == null || !SystemEventConstant.SETTING_CHANGED.equals(event.getEventType())) {
            return;
        }
        Object payload = event.getPayload();
        if (payload instanceof SystemSettingAggregate aggregate) {
            String category = aggregate.getCategory();
            if ("platform_basic".equals(category)) {
                refreshFromAggregate(aggregate);
            }
        }
    }

    /**
     * 按分类刷新加密器密钥。
     *
     * @param category 分类
     */
    private void refreshFromCategory(String category) {
        SystemSettingAggregate aggregate = systemSettingRepository.findByCategory(category);
        if (aggregate == null) {
            return;
        }
        refreshFromAggregate(aggregate);
    }

    /**
     * 从聚合中提取密钥并刷新。
     *
     * @param aggregate 聚合
     */
    private void refreshFromAggregate(SystemSettingAggregate aggregate) {
        String json = aggregate.getSettingJson();
        if (StrUtil.isBlank(json)) {
            return;
        }
        JSONObject obj = JSONObject.parseObject(json);
        String key = obj.getString("encryptionKey");
        if (StrUtil.isNotBlank(key)) {
            secretEncryptor.refreshKey(key);
            log.info("[SystemSettingsLoader] encryptionKey 已刷新");
        }
    }
}
