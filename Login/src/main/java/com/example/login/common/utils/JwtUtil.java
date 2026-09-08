package com.example.login.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    // 30分钟
    private static final long ACCESS_EXPIRE = TimeUnit.MINUTES.toMillis(30);

    // 7天
    private static final long REFRESH_EXPIRE = TimeUnit.DAYS.toMillis(7);

    // 安全密钥对象
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // 创建AccessToken令牌
    public String createAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + ACCESS_EXPIRE)
                )
                .signWith(getKey())
                .compact();
    }

    // 创建RefreshToken
    public String createRefreshToken(String username) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + REFRESH_EXPIRE)
                )
                .signWith(getKey())
                .compact();
    }

    // 解析token，判断Token是否过期，是否正常
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 返回用户名
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    // 返回类型
    public String getType(String token) {
        return parseToken(token)
                .get("type", String.class);
    }
}
