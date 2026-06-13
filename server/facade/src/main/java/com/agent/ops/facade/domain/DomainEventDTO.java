package com.agent.ops.facade.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 跨应用边界传递的领域事件载荷对象。
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DomainEventDTO {
    /**
     * 全局唯一事件标识。
     */
    private String eventId;

    /**
     * 领域事件类型。
     */
    private String eventType;

    /**
     * 产生事件的业务编码。
     */
    private String businessNum;

    /**
     * 事件发生时间。
     */
    private LocalDateTime occurredAt;

    /**
     * 触发事件的操作人业务编码。
     */
    private String operatorCode;

    /**
     * 事件特有的业务载荷。
     */
    private Object payload;


    /**
     * 返回eventId。
     *
     * @return eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 返回eventType。
     *
     * @return eventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 返回businessNum。
     *
     * @return businessNum
     */
    public String getBusinessNum() {
        return businessNum;
    }

    /**
     * 返回occurredAt。
     *
     * @return occurredAt
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * 返回operatorCode。
     *
     * @return operatorCode
     */
    public String getOperatorCode() {
        return operatorCode;
    }

    /**
     * 返回payload。
     *
     * @return payload
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * 设置eventId。
     *
     * @param eventId eventId
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 设置eventType。
     *
     * @param eventType eventType
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * 设置businessNum。
     *
     * @param businessNum businessNum
     */
    public void setBusinessNum(String businessNum) {
        this.businessNum = businessNum;
    }

    /**
     * 设置occurredAt。
     *
     * @param occurredAt occurredAt
     */
    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    /**
     * 设置operatorCode。
     *
     * @param operatorCode operatorCode
     */
    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }

    /**
     * 设置payload。
     *
     * @param payload payload
     */
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
