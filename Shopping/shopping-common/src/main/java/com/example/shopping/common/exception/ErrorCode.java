package com.example.shopping.common.exception;

/**
 * 统一错误码接口。
 *
 * 为什么定义接口，而不是项目里所有错误码都写一个巨大枚举？
 *
 * 因为以后不同业务模块可以定义自己的错误码：
 *
 * shopping-product
 *      ProductErrorCode
 *
 * shopping-order
 *      OrderErrorCode
 *
 * shopping-payment
 *      PaymentErrorCode
 *
 * 它们只需要实现 ErrorCode 接口即可。
 *
 * 这样避免一个 ErrorCode.java 最后出现几百、几千个错误码。
 */

public interface ErrorCode {


    /**
     * 业务错误码。
     *
     * 例如：
     *
     * COMMON_400_001
     * PRODUCT_404_001
     * ORDER_409_001
     */
    String getCode();


    /**
     * 给前端或者调用方看的错误描述。
     */
    String getMessage();


    /**
     * HTTP状态码。
     *
     * 例如：
     *
     * 400 参数错误
     * 404 数据不存在
     * 409 业务状态冲突
     * 500 系统异常
     */
    int getHttpStatus();

}
