package com.example.ratelimiting.common.exception;

import lombok.Getter;

// 业务限流异常：请求已经正常到达限流系统，但额度用完了。
@Getter
public class RateLimitException extends RuntimeException {
    private final long limit;

//    毫秒后重试
    private final long retryAfterMillis;

    public RateLimitException(
            String message,
            long limit,
            long retryAfterMillis
    ) {
        super(message);
        this.limit = limit;
        this.retryAfterMillis = retryAfterMillis;
    }
}
