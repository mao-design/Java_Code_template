package com.example.ratelimiting.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public DefaultRedisScript<List> slidingWindowRateLimitScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();

        script.setScriptSource(
               new ResourceScriptSource(
                       new ClassPathResource("lua/sliding-window-rate-limit.lua")
               )
        );

        script.setResultType(List.class);

        return script;
    }
}


















