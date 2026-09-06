package com.example.login123.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
* jwt拦截规则
* 拦截后要执行那个方法
* Access Token（访问令牌）
* Refresh Token（刷新令牌）
* */

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {


    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
    }
}
