package com.example.ratelimiting.common.resolver;

import com.example.ratelimiting.annotation.RateLimit;
import com.example.ratelimiting.common.utils.HashUtils;
import com.example.ratelimiting.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.DefaultParameterNameDiscoverer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//负责生成“本次请求应该使用哪个 Redis 限流 Key”。这是理解 GLOBAL / IP / SPEL 的关键类。

@Component
@RequiredArgsConstructor
public class RateLimitKeyResolver {
    private final RateLimitProperties properties;

    private final ExpressionParser expressionParser =
            new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer =
            new DefaultParameterNameDiscoverer();

    private final ConcurrentMap<String, Expression> expressionCache =
            new ConcurrentHashMap<>();

    public String resolve(
            RateLimit rateLimit,
            Method method,
            Object[] args
    ) {
        String resourceName = resolveResourceName(
                rateLimit,
                method
        );

        String identity = switch (rateLimit.keyType()) {
            case GLOBAL -> "global";
            case IP -> resolveIp();
            case SPEL -> resolveSpel(
                    rateLimit.key(),
                    method,
                    args
            );
        };

        String identityHash = HashUtils.sha256(identity);

        return properties.getKeyPrefix()
                + ":"
                + resourceName
                + ":"
                + rateLimit.keyType().name().toLowerCase()
                + ":"
                + identityHash;
    }

    private String resolveResourceName(
            RateLimit rateLimit,
            Method method
    ) {
        if (StringUtils.hasText(rateLimit.name())) {
            return normalize(rateLimit.name());
        }

        StringBuilder builder = new StringBuilder();

        builder.append(method.getDeclaringClass().getName())
                .append(".")
                .append(method.getName());

        // TODO 疑惑
        Class<?>[] parameterTypers =
                method.getParameterTypes();

        builder.append("(");

        for (int i = 0; i < parameterTypers.length; i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(parameterTypers[i].getName());
        }

        builder.append(")");

        return normalize(
                builder.toString()
        );
    }

    private String resolveIp() {
        if ( !(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)
        ) {
            throw new IllegalStateException(
                    "IP速率限制只能在HTTP请求上下文中使用"
            );
        }

        HttpServletRequest request =
                attributes.getRequest();

        String ip = request.getRemoteAddr();

        if (!StringUtils.hasText(ip)) {
            throw new IllegalStateException(
                    "无法解析客户端 IP 地址"
            );
        }

        return ip;
    }

    private String resolveSpel(
            String spel,
            Method method,
            Object[] args
    ) {
        if (!StringUtils.hasText(spel)) {
            throw new IllegalStateException(
                    "当 keyType=SPEL 时，RateLimit.key 不能为空"
            );
        }

        StandardEvaluationContext context = new StandardEvaluationContext();

        String[] parameterNames =
                parameterNameDiscoverer.getParameterNames(method);

        for (int i = 0; i < args.length; i++) {
            context.setVariable(
                    "p" + i,
                    args[i]
            );

            context.setVariable(
                    "a" + i,
                    args[i]
            );

            if (parameterNames != null
                && i < parameterNames.length) {
                context.setVariable(
                        parameterNames[i],
                        args[i]
                );
            }
        }

        Expression expression =
                expressionCache.computeIfAbsent(
                        spel,
                        expressionParser::parseExpression
                );

        Object value = expression.getValue(context);

        String identity =
                Objects.toString(value, "");

        if (!StringUtils.hasText(identity)) {
            throw new IllegalStateException(
                    "RateLimit SpEL 结果不能为空：" + spel
            );
        }

        return identity;
    }

    private String normalize(String value) {
        return value.replaceAll(
                "[^a-zA-Z0-9:_\\-.]",
                "_"
        );
    }
}
