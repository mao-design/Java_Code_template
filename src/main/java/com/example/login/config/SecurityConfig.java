package com.example.login.config;

import com.example.login.common.utils.JwtFilter;
import com.example.login.common.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.example.login.enums.ErrorCode.*;

// 配置拦截接口，拦截的接口传到其他对象
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/auth/login",
                                "/user/auth/refresh",
                                // Knife4j
                                "/doc.html",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                // 401、403统一返回Result
                .exceptionHandling(exception -> exception

                        // 未登录 / Token无效
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Result.error(
                                            NOT_LOGIN.getCode(),
                                            "用户未登录或登录已过期"
                                    )
                            );
                        })

                        // 没权限
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Result.error(
                                            NO_PERMISSION.getCode(),
                                            NO_PERMISSION.getMessage()
                                    )
                            );
                        })
                )
                // 控制 Spring Security 过滤器链中的执行顺序
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
