package com.example.ratelimiting.enums;

public enum RateLimitKeyType {
    /**
     * 整个接口共享一个限流窗口。
     */
    GLOBAL,

    /**
     * 根据客户端 IP 限流。
     */
    IP,

    /**
     * 根据 SpEL 表达式结果限流。
     *
     * 例如：
     * #userId
     * #request.userId
     * #order.userId
     */
    SPEL
}
