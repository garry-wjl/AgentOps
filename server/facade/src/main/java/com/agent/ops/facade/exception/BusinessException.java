package com.agent.ops.facade.exception;

/**
 * 携带业务错误码的运行时异常，用于统一接口错误映射。
 */
public class BusinessException extends RuntimeException {
    /**
     * 异常序列化兼容标识。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 业务错误码。
     */
    private final String code;

    /**
     * 使用默认错误码创建业务异常。
     *
     * @param message 业务错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = "500";
    }

    /**
     * 使用指定错误码创建业务异常。
     *
     * @param code 业务错误码
     * @param message 业务错误消息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code == null ? "500" : code;
    }

    /**
     * 使用默认错误码和根因异常创建业务异常。
     *
     * @param message 业务错误消息
     * @param cause 根因异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "500";
    }

    /**
     * 返回业务错误码。
     *
     * @return 业务错误码
     */
    public String getCode() {
        return code;
    }
}
