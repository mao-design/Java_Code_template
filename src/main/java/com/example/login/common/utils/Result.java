package com.example.login.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.login.enums.ErrorCode.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private String code;
    private String message;
    private T data;

    public static  Result success() {
        return new Result (
                SUCCESS.getCode(),
                SUCCESS.getMessage(),
                null
        );
    }
    public static <T> Result<T> success(T data) {
        return new Result<>(
                SUCCESS.getCode(),
                SUCCESS.getMessage(),
                data
        );
    }
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(
                SUCCESS.getCode(),
                message,
                data
        );
    }
    public static <T> Result<T> success(String code, String message, T data) {
        return new Result<>(
                code,
                message,
                data
        );
    }

    public static <T> Result<T> error(String code, String message) {
        return new Result<>(
                code,
                message,
                null
        );
    }
}
