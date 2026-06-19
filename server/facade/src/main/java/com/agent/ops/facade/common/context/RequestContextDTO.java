package com.agent.ops.facade.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 在适配层与应用层之间传递的请求上下文。所有跨领域引用均使用业务编码（String）。
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestContextDTO {
    /**
     * 当前认证用户的业务编码（user.num）。
     */
    private String currentUserCode;

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
    private String spaceCode;
}
