package com.agent.ops.facade.domain;

import java.util.List;

/**
 * 供领域对象发布领域事件的通用契约。
 */
public interface DomainEventPublisher {
    /**
     * 发布单个领域事件。
     *
     * @param event 待发布的领域事件
     */
    void publish(DomainEventDTO event);

    /**
     * 按集合顺序批量发布领域事件。
     *
     * @param events 待发布的领域事件集合
     */
    default void publishAll(List<DomainEventDTO> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        events.forEach(this::publish);
    }
}
