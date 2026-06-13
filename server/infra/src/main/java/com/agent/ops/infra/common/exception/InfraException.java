package com.agent.ops.infra.common.exception;

/**
 * 基础设施层运行时异常。
 */
public class InfraException extends RuntimeException {
    /**
     * 异常序列化兼容标识。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 创建基础设施层异常。
     *
     * @param message 异常消息
     */
    public InfraException(String message) {
        super(message);
    }

    /**
     * 创建带根因的基础设施层异常。
     *
     * @param message 异常消息
     * @param cause 根因异常
     */
    public InfraException(String message, Throwable cause) {
        super(message, cause);
    }
}
