package com.example.ratelimiting.service.impl;

import com.example.ratelimiting.common.exception.RateLimitInfrastructureException;
import com.example.ratelimiting.model.RateLimitResult;
import com.example.ratelimiting.service.RedisRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// Java 与 Lua 之间的桥梁。它负责准备参数、执行 Redis Script、解析 Lua 返回值并生成 `RateLimitResult`
@Service
@RequiredArgsConstructor
public class RedisRateLimitServiceImpl implements RedisRateLimitService {

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<List> slidingWindowRateLimitScript;

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequests, long windowMillis) {
        String member = UUID.randomUUID()
                .toString()
                .replace("-", "");

        try {
            List<?> result =
                    redisTemplate.execute(
                            slidingWindowRateLimitScript,
                            Collections.singletonList(key),
                            String.valueOf(maxRequests),
                            String.valueOf(windowMillis),
                            member
                    );

            if (result == null || result.size() < 4) {
                throw new RateLimitInfrastructureException(
                        "Redis速率限制结果无效",
                        null
                );
            }

            long allowed = parseLong(result.get(0));
            long currentCount = parseLong(result.get(1));
            long retryAfterMillis = parseLong(result.get(2));
            long serverTimestampMillis = parseLong(result.get(3));
            long remaining = Math.max(
                    maxRequests - currentCount,
                    0
            );

            return new RateLimitResult(
                    allowed == 1,
                    currentCount,
                    remaining,
                    retryAfterMillis,
                    serverTimestampMillis
            );
        } catch (RateLimitInfrastructureException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new RateLimitInfrastructureException(
                    "Redis 速率限制器不可用",
                    e
            );
        } catch (RuntimeException e) {
            // Redis/Lua 返回值格式异常也属于限流基础设施异常，
            // 统一包装后，RateLimitAspect 中的 failOpen 才能生效。
            throw new RateLimitInfrastructureException(
                    "无效的 Redis 速率限制响应",
                    e
            );
        }
    }

    private long parseLong(Object value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Redis 速率限制结果项不能为空"
            );
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof byte[] bytes) {
            return Long.parseLong(
                    new String(
                            bytes,
                            StandardCharsets.UTF_8
                    )
            );
        }

        return Long.parseLong(
                value.toString()
        );
    }
}
