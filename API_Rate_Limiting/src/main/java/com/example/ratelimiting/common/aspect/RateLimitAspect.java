package com.example.ratelimiting.common.aspect;

import com.example.ratelimiting.annotation.RateLimit;
import com.example.ratelimiting.common.exception.RateLimitException;
import com.example.ratelimiting.common.exception.RateLimitInfrastructureException;
import com.example.ratelimiting.common.resolver.RateLimitKeyResolver;
import com.example.ratelimiting.model.RateLimitResult;
import com.example.ratelimiting.service.RedisRateLimitService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedisRateLimitService redisRateLimitService;

    private final RateLimitKeyResolver rateLimitKeyResolver;

    @Around( "@annotation(com.example.ratelimiting.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = resolveMethod(joinPoint);

        RateLimit rateLimit =
                AnnotatedElementUtils.findMergedAnnotation(
                        method,
                        RateLimit.class
                );

        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        validate(rateLimit);

        long windowMillis =
                rateLimit.timeUnit()
                        .toMillis(
                                rateLimit.window()
                        );

        String key = rateLimitKeyResolver.resolve(
                rateLimit,
                method,
                joinPoint.getArgs()
        );

        RateLimitResult result;

        try {
            result = redisRateLimitService.tryAcquire(
                    key,
                    rateLimit.maxRequests(),
                    windowMillis
            );
        } catch (RateLimitInfrastructureException e) {
            if (rateLimit.failOpen()) {
                return joinPoint.proceed();
            }

            throw e;
        }

        writeAllowedHeaders(
                rateLimit,
                windowMillis,
                result
        );

        if (!result.allowed()) {
            throw new RateLimitException(
                    rateLimit.message(),
                    rateLimit.maxRequests(),
                    result.retryAfterMillis()
            );
        }

        return joinPoint.proceed();
    }






    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        Method specificMethod = AopUtils.getMostSpecificMethod(
                method,
                joinPoint.getTarget().getClass()
        );

        return BridgeMethodResolver
                .findBridgedMethod(specificMethod);
    }

    private void validate(RateLimit rateLimit) {
        if (rateLimit.maxRequests() <= 0) {
            throw new IllegalArgumentException(
                    "RateLimit.maxRequests 必须大于 0"
            );
        }

        if (rateLimit.window() <= 0) {
            throw new IllegalArgumentException(
                    "RateLimit.window 必须大于 0"
            );
        }

        if (rateLimit.timeUnit().toMillis(rateLimit.window()) <= 0) {
            throw new IllegalArgumentException(
                    "RateLimit 窗口 Millis 必须大于 0"
            );
        }
    }


    private void writeAllowedHeaders(
            RateLimit rateLimit,
            long windowMillis,
            RateLimitResult result
    ) {
        HttpServletResponse response = currentResponse();

        if (response == null) {
            return;
        }

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(
                        rateLimit.maxRequests()
                )
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(
                        result.remaining()
                )
        );

        response.setHeader(
                "X-RateLimit-Window-Millis",
                String.valueOf(
                        windowMillis
                )
        );
    }

    private HttpServletResponse currentResponse() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {
            return attributes.getResponse();
        }

        return null;
    }

}
