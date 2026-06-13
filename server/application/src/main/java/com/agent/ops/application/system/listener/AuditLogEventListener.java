package com.agent.ops.application.system.listener;

import cn.hutool.core.util.StrUtil;
import com.agent.ops.domain.system.AuditLogAggregate;
import com.agent.ops.domain.system.factory.AuditLogFactory;
import com.agent.ops.facade.domain.DomainEventDTO;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 审计日志事件监听器：订阅各模块领域事件，按 `<module>.<aggregate>.<action>` 拆解写入审计表。
 * <p>
 * 仅落库与领域审计相关的事件（事件类型形如 `space.space.created`），系统设置事件等内部事件不落库。
 */
@Component
public class AuditLogEventListener {
    private static final Logger log = LoggerFactory.getLogger(AuditLogEventListener.class);

    @Resource
    private AuditLogFactory auditLogFactory;

    /**
     * 监听所有领域事件，按事件类型拆 module/action 写审计。
     *
     * @param event 领域事件
     */
    @EventListener
    public void onEvent(DomainEventDTO event) {
        if (event == null || StrUtil.isBlank(event.getEventType())) {
            return;
        }
        String type = event.getEventType();
        // 跳过系统内部事件（如 system.setting.changed 仅供 Loader 用）
        if (type.startsWith("system.setting")) {
            return;
        }
        String[] parts = type.split("\\.");
        if (parts.length < 2) {
            return;
        }
        String module = parts[0];
        String action = type;
        try {
            String detailJson = event.getPayload() == null ? null : JSON.toJSONString(event.getPayload());
            AuditLogAggregate auditLog = auditLogFactory.create(
                    module,
                    action,
                    StrUtil.blankToDefault(event.getOperatorCode(), "SYSTEM"),
                    event.getBusinessNum(),
                    detailJson
            );
            auditLog.save(StrUtil.blankToDefault(event.getOperatorCode(), "SYSTEM"));
        } catch (Exception e) {
            log.warn("[AuditLogEventListener] 写入审计日志失败 type={} businessNum={}: {}",
                    type, event.getBusinessNum(), e.getMessage());
        }
    }
}
