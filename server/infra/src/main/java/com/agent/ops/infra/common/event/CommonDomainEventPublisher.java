package com.agent.ops.infra.common.event;

import com.agent.ops.facade.domain.DomainEventDTO;
import com.agent.ops.facade.domain.DomainEventPublisher;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring 应用事件机制的领域事件发布器实现。
 */
@Component
public class CommonDomainEventPublisher implements DomainEventPublisher {
    /**
     * Spring 应用事件发布器。
     */
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布单个领域事件。
     *
     * @param event 待发布的领域事件
     */
    @Override
    public void publish(DomainEventDTO event) {
        applicationEventPublisher.publishEvent(event);
    }
}
