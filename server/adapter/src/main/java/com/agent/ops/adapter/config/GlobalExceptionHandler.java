package com.agent.ops.adapter.config;

import com.agent.ops.facade.common.Result;
import com.agent.ops.facade.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，将各类异常统一封装为 Result 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 处理系统未知异常。
     *
     * @param e 系统异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("系统异常", e);
        String msg = e.getMessage() != null ? e.getMessage() : "系统异常";
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.fail(msg));
    }
}