package com.agent.ops.facade.request;

import lombok.Data;

/**
 * 携带通用请求上下文字段的请求基类。所有跨领域引用使用业务编码（String）。
 */
@Data
public class CommonRequest {
    /**
     * 从认证上下文解析得到的当前操作人业务编码。
     */
    private String operatorCode;

    /**
     * 返回 operatorCode。
     *
     * @return 当前操作人业务编码
     */
    public String getOperatorCode() {
        return operatorCode;
    }

    /**
     * 设置 operatorCode。
     *
     * @param operatorCode 当前操作人业务编码
     */
    public void setOperatorCode(String operatorCode) {
        this.operatorCode = operatorCode;
    }
}
