package com.example.login.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "登录返回")
public class LoginTokenVO {
    @Schema(description = "访问Token")
    private String accessToken;


    @Schema(description = "刷新Token")
    private String refreshToken;
}
