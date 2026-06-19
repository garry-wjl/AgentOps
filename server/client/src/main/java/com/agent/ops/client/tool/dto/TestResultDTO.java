package com.agent.ops.client.tool.dto;

import lombok.Data;

import java.util.Map;

/**
 * 试运行结果 DTO。
 */
public class TestResultDTO {
    public Boolean success;
    public Long durationMs;
    /**
     * 请求详情：method/url/headers/body。
     */
    public Map<String, Object> request;
    /**
     * 响应：status/headers/body。
     */
    public Map<String, Object> response;
    public String errorMessage;
}
