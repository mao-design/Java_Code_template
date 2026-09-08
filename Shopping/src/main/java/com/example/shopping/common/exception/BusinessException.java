package com.example.shopping.common.exception;

import lombok.Getter;

import static com.example.login.enums.ErrorCode.*;

@Getter
public class BusinessException extends RuntimeException {
    /** 错误码 */
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = SYSTEM_ERROR.getCode();
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = SYSTEM_ERROR.getCode();
    }

    /**
     * 快捷构造：参数错误
     */
    public static BusinessException param(String message) {
        return new BusinessException(PARAM_ERROR.getCode(), message);
    }

    /**
     * 快捷构造：未找到
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(DATA_NOT_EXIST.getCode(), message);
    }
}
