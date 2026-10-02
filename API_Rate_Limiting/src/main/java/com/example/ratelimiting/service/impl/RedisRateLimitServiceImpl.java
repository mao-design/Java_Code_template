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

    // com.example.ratelimiting.config.RateLimitConfig 创建调用 Lua 脚本的对象
    private final DefaultRedisScript<List> slidingWindowRateLimitScript;

    @Override
    public RateLimitResult tryAcquire(String key, int maxRequests, long windowMillis) {

        // 随机 UUID
        String member = UUID.randomUUID()
                .toString()
                // 代替，将 "-" 换成 ""
                // 相当于去掉 "-"
                .replace("-", "");

        try {
            /*
            *   redisTemplate.execute(
                    要执行哪个 Lua 脚本,
                    KEYS 参数,
                    ARGV[1],
                    ARGV[2],
                    ARGV[3]
                )
            * */
            List<?> result =
                    redisTemplate.execute(
                            slidingWindowRateLimitScript,
                            // Collections.singletonList(x) 就是快速创建一个只有一个元素、不可修改、可含 null 的 List
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

            // 判断当前请求是否允许
            // 1 = 允许当前请求
            // 0 = 拒绝当前请求
            long allowed = parseLong(result.get(0));

            // 当前滑动窗口内已经统计到的请求数量
            long currentCount = parseLong(result.get(1));

            // 如果当前请求被拒绝，还需要等待多少毫秒才能再次尝试
            long retryAfterMillis = parseLong(result.get(2));

            // Redis 服务器当前时间戳，单位毫秒。
            long serverTimestampMillis = parseLong(result.get(3));

            // 当前窗口还剩多少次可用额度
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

    // 统一转换成 Long 类型
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
