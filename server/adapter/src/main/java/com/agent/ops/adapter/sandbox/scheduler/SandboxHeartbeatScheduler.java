package com.agent.ops.adapter.sandbox.scheduler;

import com.agent.ops.application.sandbox.command.SandboxCommandService;
import com.agent.ops.application.sandbox.query.SandboxQueryService;
import com.agent.ops.infra.common.lock.RedisDistributedLock;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 沙箱探活调度器：每 30s 一次，多实例部署时通过 Redis 锁选举单实例运行。
 */
@Component
public class SandboxHeartbeatScheduler {
    private static final Logger log = LoggerFactory.getLogger(SandboxHeartbeatScheduler.class);

    /**
     * 调度器锁 key。
     */
    private static final String SCHEDULER_LOCK_KEY = "scheduler:sandbox-heartbeat";

    @Resource
    private SandboxQueryService sandboxQueryService;

    @Resource
    private SandboxCommandService sandboxCommandService;

    @Resource
    private RedisDistributedLock distributedLock;

    /**
     * 每 30s 执行一次。
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void heartbeat() {
        try {
            distributedLock.execute(SCHEDULER_LOCK_KEY, 1L, 60L, () -> {
                List<String> nums = sandboxQueryService.listProbeable();
                if (nums == null || nums.isEmpty()) {
                    return null;
                }
                log.info("[SandboxHeartbeatScheduler] probing {} sandboxes", nums.size());
                for (String num : nums) {
                    try {
                        sandboxCommandService.runHeartbeat(num);
                    } catch (Exception e) {
                        log.warn("[SandboxHeartbeatScheduler] probe sandbox {} failed: {}", num, e.getMessage());
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.debug("[SandboxHeartbeatScheduler] skip this round (lock not acquired or error): {}", e.getMessage());
        }
    }
}
