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
        //
        String resourceName = resolveResourceName(
                rateLimit,
                method
        );

        String identity = switch (rateLimit.keyType()) {
            case GLOBAL -> "global";
            case IP -> resolveIp(); // 返回 ip
            case SPEL -> resolveSpel( // 返回 value 值
                    rateLimit.key(),
                    method,
                    args
            );
        };

        // 转换成 hash
        String identityHash = HashUtils.sha256(identity);

        return properties.getKeyPrefix()
                + ":"
                + resourceName
                + ":"
                + rateLimit.keyType().name().toLowerCase() // 速率限制密钥类型名称（小写）
                + ":"
                + identityHash; // identity 哈希值
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
        // RequestContextHolder.getRequestAttributes() 获取当前线程关联的 Web 请求上下文
        // instanceof ServletRequestAttributes attributes 判断当前这个请求上下文是不是 Servlet HTTP 请求上下文？
        if ( !(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)
        ) {
            throw new IllegalStateException(
                    "IP速率限制只能在HTTP请求上下文中使用"
            );
        }

        /*
        * 这里的 attributes 是前面拿到的 ServletRequestAttributes。
        attributes.getRequest() 会取出当前这一次 HTTP 请求对应的 HttpServletRequest
        * */
        HttpServletRequest request =
                attributes.getRequest();
        /*
        *   request.getMethod();      // GET / POST
            request.getRequestURI();  // 请求路径
            request.getHeader(...);   // 请求头
            request.getRemoteAddr();  // 对端 IP
        * */


        String ip = request.getRemoteAddr();

        /*
        *   StringUtils.hasText(ip) 是 Spring 提供的字符串判断方法。
            它会判断这个字符串：
            不是 null
            不是 ""
            不是 " " 这种只有空格的字符串
        * */
        if (!StringUtils.hasText(ip)) {
            throw new IllegalStateException(
                    "无法解析客户端 IP 地址"
            );
        }

        return ip;
    }


    private String resolveSpel(
            // rateLimit.key()
            String spel,
            // 参数名
            Method method,
            // 参数值
            Object[] args
    ) {
        // 判断是否为空
        if (!StringUtils.hasText(spel)) {
            throw new IllegalStateException(
                    "当 keyType=SPEL 时，RateLimit.key 不能为空"
            );
        }

        // 创建一个 SpEL 的“表达式执行环境”
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 获取参数名，有多个参数时则将多个参数名保存在数组中
        // public void test(Long userId, String businessType)
        // userId, businessType
        String[] parameterNames =
                parameterNameDiscoverer.getParameterNames(method);

        for (int i = 0; i < args.length; i++) {
            /*
            * 传值 test(10001L, "order");
            * 那么
            *   args[0] = 10001L;
                args[1] = "order";
            *
            * 当 i = 0 时
            * context.setVariable("p0", 10001L);
            * */
            context.setVariable(
                    "p" + i,
                    args[i]
            );

            context.setVariable(
                    "a" + i,
                    args[i]
            );

            // 判断是否获取到方法名
            if (parameterNames != null
                && i < parameterNames.length) {
                // 当 i = 0 时
                // context.setVariable("userId", 10001L);
                context.setVariable(
                        // 参数名
                        parameterNames[i],
                        // 参数值
                        args[i]
                );
            }
        }

        /*
        *   先去 expressionCache 里找 "#userId"
                    ↓
            找到了
                    ↓
            直接使用已经解析好的 Expression

            ==================================

        *   没有 "#userId"
                    ↓
            expressionParser.parseExpression("#userId")
                    ↓
            生成 Expression
                    ↓
            放进缓存
                    ↓
            以后重复使用
        * */
        Expression expression =
                // 避免每次请求都重复解析 SpEL
                // expressionParser.parseExpression(spel)
                expressionCache.computeIfAbsent(
                        spel,
                        expressionParser::parseExpression
                );

        // 根据 spel 值获取对应的 value
        // public void test(Long userId, String businessType)
        // 往 userId 中传入 10001L
        // value = 10001L
        Object value = expression.getValue(context);

        // 将 value 转换为 String
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
