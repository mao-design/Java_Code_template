package com.example.ratelimiting.controller;

import com.example.ratelimiting.annotation.RateLimit;
import com.example.ratelimiting.enums.RateLimitKeyType;
import com.example.ratelimiting.model.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/demo")
public class RateLimitDemoController {

    /**
     * 整个接口：
     * 10 秒最多访问 5 次。
     */
    @GetMapping("/global")
    @RateLimit(
            name = "demo:global",
            keyType = RateLimitKeyType.GLOBAL,
            maxRequests = 5,
            window = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public Map<String, Object> global() {
        return Map.of(
                "message", "global success",
                "time", Instant.now()
        );
    }

    /**
     * 每个 IP：
     * 10 秒最多访问 5 次。
     */
    @GetMapping("/ip")
    @RateLimit(
            name = "demo:ip",
            keyType = RateLimitKeyType.IP,
            maxRequests = 5,
            window = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public Map<String, Object> ip() {
        return Map.of(
                "message", "global success",
                "time", Instant.now()
        );
    }

    /**
     * 每个 userId：
     * 30 秒最多访问 3 次。
     */
    @GetMapping("/user/{userId}")
    @RateLimit(
            name = "demo:user",
            keyType = RateLimitKeyType.SPEL,
            key = "#userId",
            maxRequests = 3,
            window = 30,
            timeUnit = TimeUnit.SECONDS
    )
    public Map<String, Object> user(
            @PathVariable Long userId
    ) {

        return Map.of(
                "userId", userId,
                "message", "user success",
                "time", Instant.now()
        );
    }

    /**
     * 每个 username：
     * 1 分钟最多尝试登录 5 次。
     */
    @PostMapping("/login")
    @RateLimit(
            name = "demo:login",
            keyType = RateLimitKeyType.SPEL,
            key = "#request.username",
            maxRequests = 5,
            window = 1,
            timeUnit = TimeUnit.MINUTES,
            failOpen = false,
            message = "登录尝试次数过多，请稍后再试"
    )
    public Map<String, Object> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return Map.of(
                "username", request.username(),
                "message", "login success",
                "time", Instant.now()
        );
    }

    /**
     * 同一个 userId + businessType：
     * 60 秒最多请求 10 次。
     */
    @GetMapping("/business")
    @RateLimit(
            name = "demo:business",
            keyType = RateLimitKeyType.SPEL,
            key = "#userId + ':' + #businessType",
            maxRequests = 10,
            window = 60,
            timeUnit = TimeUnit.SECONDS
    )
    public Map<String, Object> business(
            @RequestParam Long userId,
            @RequestParam String businessType
    ) {

        return Map.of(
                "userId", userId,
                "businessType", businessType,
                "message", "business success",
                "time", Instant.now()
        );
    }


}
