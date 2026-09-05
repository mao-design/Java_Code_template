package com.example.login.controller;

import com.example.login.common.utils.Result;
import com.example.login.model.dto.LoginDTO;
import com.example.login.model.dto.RefreshDTO;
import com.example.login.model.dto.RegisterDTO;
import com.example.login.model.vo.LoginTokenVO;
import com.example.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
@Tag(
        name = "用户认证",
        description = "登录、注册、刷新Token、查看个人信息"
)
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "用户登录",
            description = "账号密码登录，返回双JWT"
    )
    @PostMapping("/login")
    public Result<LoginTokenVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(
                userService.login(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );
    }

    @Operation(
            summary = "刷新Token",
            description = "刷新Token，返回双JWT"
    )
    @PostMapping("/refresh")
    public Result<LoginTokenVO> refresh(@RequestBody RefreshDTO refreshDTO) {
        return Result.success(
                userService.refresh(
                        refreshDTO.getRefreshToken()
                )
        );
    }

    @Operation(
            summary = "注册新用户",
            description = "注册新用户"
    )
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }

    @Operation(
            summary = "退出登录",
            description = "退出登录"
    )
    @PostMapping("/logout")
    public Result logout(@RequestBody RefreshDTO refreshDTO) {
        userService.logout(
                refreshDTO.getRefreshToken()
        );
        return Result.success("退出成功");
    }
}
