package com.example.ratelimiting.common.exception;



/*
* 作用
限流基础设施异常，例如 Redis 不可用、Lua 返回值异常。
* */
public class RateLimitInfrastructureException extends RuntimeException {

    public RateLimitInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
