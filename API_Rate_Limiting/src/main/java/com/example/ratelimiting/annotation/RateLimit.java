package com.example.ratelimiting.annotation;

import com.example.ratelimiting.enums.RateLimitKeyType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流资源名称。
     *
     * name 用来标识限流资源。
     *
     * 注意：
     * 最终 Redis Key 还会包含 keyType 和 identity，
     * 因此只有 name、keyType、identity 都相同的请求才真正共享同一个限流桶。
     */
    String name() default "";

    /**
     * SPEL 类型时使用。
     *
     * 例如：
     * #userId
     * #request.userId
     */
    String key() default "";

    /**
     * 限流维度。
     */
    RateLimitKeyType keyType() default RateLimitKeyType.IP;

    /**
     * 滑动窗口内最大请求次数。
     */
    int maxRequests();

    /**
     * 窗口大小。
     */
    long window();

    /**
     * 窗口时间单位。
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Redis 异常时是否放行。
     *
     * true:
     * Redis 挂了不影响核心业务。
     *
     * false:
     * Redis 挂了直接拒绝。
     */
    boolean failOpen() default true;

    /**
     * 被限流后的提示。
     */
    String message() default "请求过于频繁，请稍后再试";

}
