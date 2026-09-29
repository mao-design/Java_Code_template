package com.example.shopping.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统公共错误码。
 *
 * 注意：
 *
 * 这里只存所有模块都会遇到的错误。
 *
 * 商品相关错误：
 *      ProductErrorCode
 *
 * 库存相关错误：
 *      InventoryErrorCode
 *
 * 订单相关错误：
 *      OrderErrorCode
 *
 * 不要全部堆到这里。
 */
@Getter // 生成三个 getter,正好实现接口中的方法
@AllArgsConstructor // 有参构造器，正好 enum 可以通过构造器传入参数
public enum CommonErrorCode implements ErrorCode {
    /**
     * 请求参数错误。
     *
     * 例如：
     *
     * name为空
     * page小于1
     * JSON格式错误
     */
    PARAM_ERROR(
            "COMMON_400_001",
            "请求参数不正确",
            400
    ),


    /**
     * 请求的数据不存在。
     *
     * 这是通用404。
     *
     * 具体业务最好定义自己的错误码，例如：
     *
     * PRODUCT_NOT_FOUND
     */
    NOT_FOUND(
            "COMMON_404_001",
            "请求的数据不存在",
            404
    ),


    /**
     * 当前业务状态不允许执行操作。
     *
     * HTTP 409 Conflict。
     */
    BUSINESS_CONFLICT(
            "COMMON_409_001",
            "当前业务状态不允许执行该操作",
            409
    ),


    /**
     * 系统未知异常。
     *
     * 例如：
     *
     * NullPointerException
     * 第三方组件异常
     * 未预料到的程序错误
     */
    SYSTEM_ERROR(
            "COMMON_500_001",
            "系统繁忙，请稍后重试",
            500
    );

    /**
     * 业务错误码。
     */
    private final String code;


    /**
     * 错误提示。
     */
    private final String message;


    /**
     * HTTP状态码。
     */
    private final int httpStatus;

}
