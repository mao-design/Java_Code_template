package com.example.ratelimiting.common.exception;

import com.example.ratelimiting.common.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result> handleRateLimitException(
            RateLimitException exception,
            HttpServletRequest request
    ) {

        long retryAfterSeconds =
                Math.max(
                        1,
                        (exception.getRetryAfterMillis() + 999) / 1000
                );

        Result body =
                new Result(
                        "429",
                        exception.getMessage(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(
                        HttpHeaders.RETRY_AFTER,
                        String.valueOf(
                                retryAfterSeconds
                        )
                )
                .header(
                        "X-RateLimit-Limit",
                        String.valueOf(
                                exception.getLimit()
                        )
                )
                .header(
                        "X-RateLimit-Remaining",
                        "0"
                )
                .body(body);
    }

    @ExceptionHandler(
            RateLimitInfrastructureException.class
    )
    public ResponseEntity<Result> handleRateLimitInfrastructureException(
            RateLimitInfrastructureException exception,
            HttpServletRequest request
    ) {

        Result body =
                new Result(
                        "503",
                        "限流服务暂时不可用",
                        null
                );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(body);
    }
}