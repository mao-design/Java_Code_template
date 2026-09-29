package com.example.shopping.common.exception;

import lombok.Getter;


/**
 * 业务异常。
 *
 * 业务异常 ≠ 系统异常。
 *
 * 例如下面这些属于业务异常：
 *
 * 商品不存在
 * 商品已经下架
 * 库存不足
 * 订单不能重复支付
 * 优惠券已经使用
 *
 *
 * Service层主动抛出：
 *
 * throw new BusinessException(
 *      ProductErrorCode.PRODUCT_NOT_FOUND
 * );
 *
 *
 * 最终由 GlobalExceptionHandler 统一处理。
 */
@Getter
public class BusinessException extends RuntimeException {


    /**
     * 具体错误码。
     */
    private final ErrorCode errorCode;


    /**
     * 使用错误码中默认的提示信息。
     */
    public BusinessException(ErrorCode errorCode) {

        super(errorCode.getMessage());

        this.errorCode = errorCode;
    }


    /**
     * 某些特殊场景允许覆盖默认提示。
     *
     * 例如：
     *
     * "商品 10001 不存在"
     *
     * 但是不要把数据库异常、SQL、堆栈信息
     * 通过这里返回给前端。
     */
    public BusinessException(
            ErrorCode errorCode,
            String message
    ) {

        super(message);

        this.errorCode = errorCode;
    }

}