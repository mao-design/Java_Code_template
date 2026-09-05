package com.example.login.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.login.common.exception.BusinessException;
import com.example.login.common.utils.JwtUtil;
import com.example.login.mapper.SysUserMapper;
import com.example.login.model.entity.SysUserDO;
import com.example.login.model.vo.LoginTokenVO;
import com.example.login.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.example.login.enums.ErrorCode.LOGIN_EXPIRED;

// 校验 refreshToken

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUserDO>
        implements UserService  {
    @Value("${app.jwt.refrshTiem}")
    private Integer refrshTiem;

    private final JwtUtil jwtUtil;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginTokenVO login(String username, String password) {

        // 查找对应的用户信息
        SysUserDO user = this.lambdaQuery()
                .eq(SysUserDO::getUsername, username).one();

        if (user == null) {
            throw new BusinessException("用户名或者密码错误");
        }

        // 用户输入的明文和数据库中加密的密文进行对比
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或者密码错误");
        }

        // 生成Token
        String accessToken = jwtUtil.createAccessToken(username);
        String refreshToken = jwtUtil.createRefreshToken(username);

        Claims claims =
                jwtUtil.parseToken(refreshToken);
        // 获取Token中username
        String userId = claims.getSubject();
        // 获取Token中username
        String jti = claims.getId();
        // 存入redis
        redisTemplate.opsForValue().set(
                "refresh:" + userId + ":" +jti,
                refreshToken,
                refrshTiem,
                TimeUnit.DAYS
        );
        // 返回给前端
        return LoginTokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 刷新refreshToken
    public LoginTokenVO refresh(String oldRefreshToken) {

        try {

            Claims oldClaims =
                    jwtUtil.parseToken(oldRefreshToken);

            if (!"refresh".equals(
                    oldClaims.get("type",String.class)
            )){
                throw new BusinessException("Token类型错误");
            }

            String username = oldClaims.getSubject();
            String oldJti = oldClaims.getId();

            String oldKey =
                    "refresh:" + username + ":" + oldJti;
            // 在redis中查找旧Token
            String oldRredisToken =
                    redisTemplate.opsForValue().get(oldKey);

            if (oldRredisToken == null) {
                throw new BusinessException("登录失效，请重新登录");
            }

            if (!oldRefreshToken.equals(oldRredisToken)) {
                throw new BusinessException("登录失效，请重新登录");
            }

            // 删除旧的Token
            redisTemplate.delete(oldKey);

            // 新 Access Token
            String newAccessToken = jwtUtil.createAccessToken(username);
            // 新 Refresh Token
            String newRefreshToken = jwtUtil.createRefreshToken(username);

            Claims newClaims = jwtUtil.parseToken(newRefreshToken);
            String newJti = newClaims.getId();

            // Redis 覆盖旧 Refresh Token
            redisTemplate.opsForValue().set(
                    "refresh:" + username + ":" + newJti,
                    newRefreshToken,
                    refrshTiem,
                    TimeUnit.DAYS
            );

            return LoginTokenVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

        } catch (Exception e) {
            throw new BusinessException(
                    LOGIN_EXPIRED.getCode(),
                    "登录已过期，请重新登录"
            );
        }
    }

    public void logout(String refreshToken) {

        try {
            Claims claims =
                    jwtUtil.parseToken(refreshToken);
            String userId = claims.getSubject();
            String jti = claims.getId();
            // 删除 Refresh Token
            redisTemplate.delete(
                    "refresh:" + userId + ":" + jti
            );
        } catch (Exception e) {

        }
    }
}
