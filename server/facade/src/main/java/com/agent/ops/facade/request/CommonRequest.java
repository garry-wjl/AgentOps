package com.agent.ops.facade.request;

import lombok.Data;

/**
 * 携带通用请求上下文字段的请求基类。
 */
@Data
public class CommonRequest {
    /**
     * 从认证上下文解析得到的当前操作人标识。
     */
    private Long operatorId;
}
