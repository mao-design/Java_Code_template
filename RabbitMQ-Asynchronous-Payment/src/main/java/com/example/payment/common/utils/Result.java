package com.example.payment.common.utils;

import com.example.payment.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.payment.enums.ErrorCode.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private String code;
    private String message;
    private T data;


    // 成功，无数据
    public static <T> Result<T> success() {
        return new Result<>(
                SUCCESS.getCode(),
                SUCCESS.getMessage(),
                null
        );
    }

    // 成功，只传提示
    public static <T> Result<T> success(String message) {
        return new Result<>(
                SUCCESS.getCode(),
                message,
                null
        );
    }

    // 成功，只传数据
    public static <T> Result<T> success(T data) {
        return new Result<>(
                SUCCESS.getCode(),
                SUCCESS.getMessage(),
                data
        );
    }

    // 成功，提示 + 数据
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(
                SUCCESS.getCode(),
                message,
                data
        );
    }

    // 失败
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    // 失败，自定义提示
    public static <T> Result<T> error(
            ErrorCode errorCode,
            String message) {

        return new Result<>(
                errorCode.getCode(),
                message,
                null
        );
    }

    // 自定义错误码
    public static <T> Result<T> error(
            String code,
            String message) {

        return new Result<>(
                code,
                message,
                null
        );
    }
}
