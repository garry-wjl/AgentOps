package com.agent.ops.application.agent.listener;

import com.agent.ops.application.agent.command.AgentCommandService;
import com.agent.ops.domain.agent.AgentVersionAggregate;
import com.agent.ops.domain.agent.event.AgentEventConstant;
import com.agent.ops.facade.domain.DomainEventDTO;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Agent 版本发布事件监听器：刷新主体的 currentVersionNo。
 * 注意：在同一事务内同步触发（依赖 save 完成后已持久化）。
 */
@Component
public class AgentVersionPublishedListener {
    private static final String SYSTEM_OPERATOR = "SYSTEM";

    @Resource
    private AgentCommandService agentCommandService;

    @EventListener
    public void onPublished(DomainEventDTO event) {
        if (event == null || !AgentEventConstant.VERSION_PUBLISHED.equals(event.getEventType())) {
            return;
        }
        if (event.getPayload() instanceof AgentVersionAggregate v) {
            String operatorCode = event.getOperatorCode() == null ? SYSTEM_OPERATOR : event.getOperatorCode();
            agentCommandService.refreshCurrentVersion(v.getAgentCode(), v.getVersionNo(), operatorCode);
        }
    }
}
