package com.agent.ops.infra.sandbox.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.agent.ops.facade.common.probe.SandboxProbeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 沙箱探活客户端实现：HTTP GET <baseUrl>/health 超时 5s。
 */
@Component
public class SandboxProbeClientImpl implements SandboxProbeClient {
    private static final Logger log = LoggerFactory.getLogger(SandboxProbeClientImpl.class);

    /**
     * 探活超时时间，单位毫秒。
     */
    private static final int TIMEOUT_MS = 5000;

    @Override
    public boolean probe(String baseUrl) {
        if (StrUtil.isBlank(baseUrl)) {
            return false;
        }
        String url = baseUrl.endsWith("/") ? baseUrl + "health" : baseUrl + "/health";
        try {
            String body = HttpUtil.createGet(url).timeout(TIMEOUT_MS).execute().body();
            return StrUtil.isNotBlank(body);
        } catch (Exception e) {
            log.debug("[SandboxProbeClient] probe failed url={} err={}", url, e.getMessage());
            return false;
        }
    }
}
