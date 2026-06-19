package com.agent.ops.application.skill.listener;

import com.agent.ops.application.skill.command.SkillCommandService;
import com.agent.ops.domain.skill.SkillVersionAggregate;
import com.agent.ops.domain.skill.event.SkillEventConstant;
import com.agent.ops.facade.domain.DomainEventDTO;
import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Skill 版本发布事件监听器：刷新主体的 currentVersionNo。
 */
@Component
public class SkillVersionPublishedListener {
    /**
     * 系统操作人占位。
     */
    private static final String SYSTEM_OPERATOR = "SYSTEM";

    @Resource
    private SkillCommandService skillCommandService;

    @EventListener
    public void onPublished(DomainEventDTO event) {
        if (event == null || !SkillEventConstant.VERSION_PUBLISHED.equals(event.getEventType())) {
            return;
        }
        if (event.getPayload() instanceof SkillVersionAggregate v) {
            String operatorCode = event.getOperatorCode() == null ? SYSTEM_OPERATOR : event.getOperatorCode();
            skillCommandService.refreshCurrentVersion(v.getSkillCode(), v.getVersionNo(), operatorCode);
        }
    }
}
