package com.example.shopping.common.result;

import com.example.shopping.common.exception.ErrorCode;
import lombok.Getter;


/**
 * REST API统一响应对象。
 *
 * Controller以后不要出现各种不同格式：
 *
 * {
 *     "status": 1
 * }
 *
 * {
 *     "code": 200
 * }
 *
 * {
 *     "success": true
 * }
 *
 *
 * 整个商城统一：
 *
 * {
 *     "code": "0",
 *     "message": "success",
 *     "data": {}
 * }
 *
 *
 * @param <T> 返回的数据类型
 */
@Getter
public class Result<T> {


    /**
     * 成功业务码。
     *
     * HTTP 200和业务成功码不是一个概念。
     */
    private static final String SUCCESS_CODE = "0";


    /**
     * 业务码。
     *
     * 0表示成功。
     *
     * 失败例如：
     *
     * PRODUCT_404_001
     */
    private final String code;


    /**
     * 返回信息。
     */
    private final String message;


    /**
     * 真正的业务数据。
     */
    private final T data;


    /**
     * 不允许业务代码随便new Result。
     *
     * 所有Result统一通过：
     *
     * success()
     * fail()
     *
     * 创建。
     */
    private Result(
            String code,
            String message,
            T data
    ) {

        this.code = code;

        this.message = message;

        this.data = data;
    }


    /**
     * 成功，没有返回数据。
     *
     * 例如：
     *
     * 删除商品
     * 修改密码
     */
    public static Result<Void> success() {

        return new Result<>(
                SUCCESS_CODE,
                "success",
                null
        );
    }


    /**
     * 成功，并返回数据。
     *
     * 例如：
     *
     * Result.success(productVO)
     *
     * Result.success(productId)
     */
    public static <T> Result<T> success(T data) {

        return new Result<>(
                SUCCESS_CODE,
                "success",
                data
        );
    }


    /**
     * 根据统一错误码创建失败响应。
     */
    public static Result<Void> fail(ErrorCode errorCode) {

        return new Result<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }


    /**
     * 使用指定错误信息。
     *
     * 主要给业务异常处理器使用。
     */
    public static Result<Void> fail(
            ErrorCode errorCode,
            String message
    ) {

        return new Result<>(
                errorCode.getCode(),
                message,
                null
        );
    }


    /**
     * 根据code和message创建失败结果。
     *
     * 一般不要在普通业务代码中到处调用这个方法。
     */
    public static Result<Void> fail(
            String code,
            String message
    ) {

        return new Result<>(
                code,
                message,
                null
        );
    }

}
