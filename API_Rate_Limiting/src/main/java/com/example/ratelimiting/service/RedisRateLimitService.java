package com.example.ratelimiting.service;

import com.example.ratelimiting.model.RateLimitResult;

public interface RedisRateLimitService {
    RateLimitResult tryAcquire(
            String key,
            int maxRequests,
            long windowMillis
    );
}
