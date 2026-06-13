package com.agent.ops.facade.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 外部接口统一响应包装对象。
 *
 * @param <T> 响应载荷类型
 */
@Getter
@Setter
public class Result<T> {
    /**
     * 标准成功响应码。
     */
    public static final String SUCCESS_CODE = "0";

    /**
     * 默认异常响应码。
     */
    public static final String DEFAULT_ERROR_CODE = "500";

    /**
     * 业务响应码。
     */
    private String code;

    /**
     * 面向调用方展示的响应消息。
     */
    private String message;

    /**
     * 响应业务数据。
     */
    private T data;

    /**
     * 用于关联日志与接口响应的链路追踪标识。
     */
    private String traceId;

    /**
     * 构建不包含业务数据的成功响应。
     *
     * @param <T> 响应载荷类型
     * @return 成功响应对象
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 构建包含业务数据的成功响应。
     *
     * @param data 响应业务数据
     * @param <T> 响应载荷类型
     * @return 成功响应对象
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS_CODE;
        result.message = "success";
        result.data = data;
        return result;
    }

    /**
     * 使用默认异常响应码构建失败响应。
     *
     * @param message 失败响应消息
     * @param <T> 响应载荷类型
     * @return 失败响应对象
     */
    public static <T> Result<T> fail(String message) {
        return fail(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 使用指定异常响应码构建失败响应。
     *
     * @param code 失败响应码
     * @param message 失败响应消息
     * @param <T> 响应载荷类型
     * @return 失败响应对象
     */
    public static <T> Result<T> fail(String code, String message) {
        Result<T> result = new Result<>();
        result.code = code == null ? DEFAULT_ERROR_CODE : code;
        result.message = message;
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
