package com.agent.ops.infra.common.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Iterator;

/**
 * 访问令牌创建与撤销基础设施工具，使用 Redis 存储 token→userCode 映射。
 */
@Component
public class TokenProvider {
    /**
     * Redis 客户端。
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 令牌有效期秒数，默认 7200。
     */
    @Value("${agentops.auth.token-ttl-seconds:7200}")
    private long tokenTtlSeconds;

    /**
     * Token 在 Redis 中的 key 前缀。
     */
    private static final String TOKEN_KEY_PREFIX = "agentops:auth:token:";

    /**
     * 用户 → token 集合的 Redis key 前缀。
     */
    private static final String USER_TOKENS_KEY_PREFIX = "agentops:auth:user-tokens:";

    /**
     * 创建访问令牌并写入 Redis。
     *
     * @param userCode 用户业务编码（user.num）
     * @return 访问令牌
     */
    public String createAccessToken(String userCode) {
        Assert.notBlank(userCode, "用户业务编码不能为空");
        String token = IdUtil.fastSimpleUUID();
        RBucket<String> bucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + token);
        bucket.set(userCode, Duration.ofSeconds(tokenTtlSeconds));
        redissonClient.getSet(USER_TOKENS_KEY_PREFIX + userCode).add(token);
        return token;
    }

    /**
     * 根据 token 解析当前用户业务编码。
     *
     * @param token 访问令牌
     * @return 用户业务编码，无效时返回 null
     */
    public String resolveUserCode(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        RBucket<String> bucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + token);
        return bucket.get();
    }

    /**
     * 撤销指定访问令牌。
     *
     * @param token 访问令牌
     */
    public void revoke(String token) {
        Assert.notBlank(token, "访问令牌不能为空");
        redissonClient.getBucket(TOKEN_KEY_PREFIX + token).delete();
    }

    /**
     * 撤销指定用户的全部访问令牌。
     *
     * @param userCode 用户业务编码
     */
    public void revokeUserTokens(String userCode) {
        Assert.notBlank(userCode, "用户业务编码不能为空");
        Iterator<String> iter = redissonClient.<String>getSet(USER_TOKENS_KEY_PREFIX + userCode).iterator();
        RKeys keys = redissonClient.getKeys();
        while (iter.hasNext()) {
            keys.delete(TOKEN_KEY_PREFIX + iter.next());
        }
        redissonClient.getSet(USER_TOKENS_KEY_PREFIX + userCode).delete();
    }
}
