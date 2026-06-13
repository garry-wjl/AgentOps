package com.agent.ops.facade.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 在适配层与应用层之间传递的请求上下文。
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestContextDTO {
    /**
     * 当前认证用户的操作人标识。
     */
    private Long operatorId;

    /**
     * 用于请求诊断的链路追踪标识。
     */
    private String traceId;

    /**
     * 预留给未来多租户场景的租户标识。
     */
    private String tenant;

    /**
     * 预留给空间级操作的空间业务编码。
     */
    private String spaceNum;
}
