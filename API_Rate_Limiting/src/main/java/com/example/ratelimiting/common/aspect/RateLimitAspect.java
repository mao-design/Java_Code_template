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

    // 匹配所有标注了该注解的方法
    @Around( "@annotation(com.example.ratelimiting.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 作用：找到是那个方法调用了注解
        Method method = resolveMethod(joinPoint);

        // 从刚才找到的 method 上获取 @RateLimit 注解对象
        // 注解中赋的值也会一并提取出来
        RateLimit rateLimit =
                AnnotatedElementUtils.findMergedAnnotation(
                        method,
                        RateLimit.class
                );

        // 没有设置AOP的接口直接放行
        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        // 判断传入值是否合规
        validate(rateLimit);

        // TimeUnit timeUnit() 时间单位
        // rateLimit.window() 时间值
        // 1分钟 == 60000 ms
        long windowMillis =
                rateLimit.timeUnit()
                        // 统一转换成毫秒
                        .toMillis(
                                rateLimit.window()
                        );

        String key = rateLimitKeyResolver.resolve(
                rateLimit,
                method,
               /*
                * 传值 test(10001L, "order");
                * 那么
                *   args[0] = 10001L;
                    args[1] = "order";
                * getArgs就是获取传入的参数的
                * */
                joinPoint.getArgs()
        );

        RateLimitResult result;

        try {
            // 判断当前请求是否频繁请求
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

        // 写入响应头 Response Header
        // 将该请求的 允许多少、还剩多少、还有多久 写入响应头 Response Header 中
        // 作用告诉客户端 “限额是多少、还剩多少、窗口多长”。
        writeAllowedHeaders(
                rateLimit,
                windowMillis,
                result
        );

        // 判断当前请求是否允许
        if (!result.allowed()) {
            throw new RateLimitException(
                    rateLimit.message(),
                    rateLimit.maxRequests(),
                    result.retryAfterMillis()
            );
        }

        // 放行该请求
        return joinPoint.proceed();
    }





    // 作用：找到是那个方法调用了注解
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

    // 判断传入值是否合规
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

    // 写入响应头 Response Header
    private void writeAllowedHeaders(
            RateLimit rateLimit,
            long windowMillis,
            RateLimitResult result
    ) {
        // 获取当前 HTTP 请求对应的响应对象 HttpServletResponse
        HttpServletResponse response = currentResponse();

        if (response == null) {
            return;
        }

        // 写入请求头
        // 当前限流窗口最多允许多少次请求
        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(
                        rateLimit.maxRequests()
                )
        );

        // 当前窗口还剩多少次请求额度
        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(
                        result.remaining()
                )
        );

        // 当前限流窗口有多长，单位毫秒
        response.setHeader(
                "X-RateLimit-Window-Millis",
                String.valueOf(
                        windowMillis
                )
        );
    }

    // 获取当前 HTTP 请求对应的响应对象 HttpServletResponse
    private HttpServletResponse currentResponse() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {
            return attributes.getResponse();
        }

        // 没有 HTTP 请求则返回 null
        return null;
    }

}
