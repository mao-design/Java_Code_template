package com.example.login.common.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.example.login.enums.ErrorCode.*;

// 普通请求只认 Access Token。
// 校验 Access Token。
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 没有 Token，直接继续交给 Spring Security
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 取Token，去掉 Bearer 剩下的就是Token值
        String token = header.substring(7);

        try {
            // 解析Token，判断Token是否正常
            Claims claims = jwtUtil.parseToken(token);

            // 只允许 Access Token
            if (!"access".equals(
                    claims.get("type", String.class))) {

                filterChain.doFilter(request, response);
                return;
            }
            // 获取Token中的username
            String username = claims.getSubject();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);


        } catch (ExpiredJwtException e) {

            // Access Token 已过期
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            Result<Void> result = Result.error(TOKEN_EXPIRED.getCode(), "Token过期");

            response.getWriter().write(objectMapper.writeValueAsString(result));

            return;
        } catch (JwtException e) {

            // Token 被修改、签名错误、格式错误等
            // 清除 Spring Security 的线程上下文，避免残留旧的认证信息造成安全问题。
            SecurityContextHolder.clearContext();
            // 401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // json格式
            response.setContentType("application/json;charset=UTF-8");

            Result<Void> result = Result.error(TOKEN_INVALID.getCode(), "Token无效");

            response.getWriter().write(objectMapper.writeValueAsString(result));
            return;
        }


        filterChain.doFilter(request, response);
    }
}
