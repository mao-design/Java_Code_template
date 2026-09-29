package com.example.ratelimiting.common.utils;

import java.time.Instant;

public record Result<T>(
    String code,

    String message,

    T data
) {
}
