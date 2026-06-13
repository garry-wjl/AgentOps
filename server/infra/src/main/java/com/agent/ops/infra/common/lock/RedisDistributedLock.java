package com.agent.ops.infra.common.lock;

import cn.hutool.core.lang.Assert;
import com.agent.ops.facade.exception.BusinessException;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的分布式锁封装，统一锁 key 前缀 `agentops:lock:`。
 */
@Component
public class RedisDistributedLock {
    /**
     * Redis 客户端。
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 锁 key 统一前缀。
     */
    private static final String LOCK_KEY_PREFIX = "agentops:lock:";

    /**
     * 默认等待获取锁时间，单位秒。
     */
    private static final long DEFAULT_WAIT_SECONDS = 3L;

    /**
     * 默认锁持有时间，单位秒。
     */
    private static final long DEFAULT_LEASE_SECONDS = 10L;

    /**
     * 在分布式锁内执行业务逻辑。锁 key 自动加 `agentops:lock:` 前缀。
     *
     * @param bizKey 业务唯一标识，最终锁 key=`agentops:lock:<bizKey>`
     * @param action 受锁保护的业务逻辑
     * @param <T>    返回值类型
     * @return 业务逻辑返回值
     */
    public <T> T execute(String bizKey, Supplier<T> action) {
        return execute(bizKey, DEFAULT_WAIT_SECONDS, DEFAULT_LEASE_SECONDS, action);
    }

    /**
     * 在分布式锁内执行业务逻辑（自定义等待与持锁时间）。
     *
     * @param bizKey       业务唯一标识
     * @param waitSeconds  等待获取锁时间，单位秒
     * @param leaseSeconds 锁持有时间，单位秒
     * @param action       受锁保护的业务逻辑
     * @param <T>          返回值类型
     * @return 业务逻辑返回值
     */
    public <T> T execute(String bizKey, long waitSeconds, long leaseSeconds, Supplier<T> action) {
        Assert.notBlank(bizKey, "锁 bizKey 不能为空");
        Assert.notNull(action, "受锁保护的业务逻辑不能为空");
        String lockKey = LOCK_KEY_PREFIX + bizKey;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("LOCK_ACQUIRE_FAILED", "操作正在进行中，请稍后再试");
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("LOCK_INTERRUPTED", "操作被中断，请重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 在分布式锁内执行无返回值业务逻辑。
     *
     * @param bizKey 业务唯一标识
     * @param action 受锁保护的业务逻辑
     */
    public void run(String bizKey, Runnable action) {
        execute(bizKey, () -> {
            action.run();
            return null;
        });
    }
}
