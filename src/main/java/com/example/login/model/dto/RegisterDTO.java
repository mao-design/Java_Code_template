package com.example.login.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须为4-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须为6-20位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须为6-20位")
    private String confirmPassword;

    private String nickname;

    private String phone;

    private String avatar;
}
