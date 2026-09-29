package com.example.ratelimiting.model;

public record RateLimitResult(
        boolean allowed,

        long currentCount,

        long remaining,

        long retryAfterMillis,

        long serverTimestampMillis
) {
}
