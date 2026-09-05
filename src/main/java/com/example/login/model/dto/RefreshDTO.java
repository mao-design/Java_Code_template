package com.example.login.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录传入参数，更新失效Access Token")
public class RefreshDTO {
    @Schema(description = "Refresh Token")
    private String refreshToken;
}
