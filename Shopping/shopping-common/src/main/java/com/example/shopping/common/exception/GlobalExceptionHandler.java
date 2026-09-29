package com.example.shopping.common.exception;

import com.example.shopping.common.result.Result;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * 全局REST异常处理器。
 *
 * Controller里面以后不要这样写：
 *
 * try {
 *
 * } catch (Exception e) {
 *
 * }
 *
 *
 * Controller只负责调用Service。
 *
 * 异常统一交给这里。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {



    /**
     * 处理业务异常。
     *
     * 例如：
     *
     * 商品不存在
     * 库存不足
     * 订单已经支付
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(
            BusinessException exception
    ) {

        ErrorCode errorCode =
                exception.getErrorCode();


        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        Result.fail(
                                errorCode,
                                exception.getMessage()
                        )
                );
    }


    /**
     * 处理 @RequestBody + @Valid 参数校验异常。
     *
     * 例如：
     *
     * public Result<?> create(
     *      @Valid @RequestBody ProductCreateDTO dto
     * )
     *
     *
     * DTO：
     *
     * @NotBlank
     * private String name;
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>>
    handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {

        /*
         * 把所有字段校验错误组合起来。
         *
         * 例如：
         *
         * name: 商品名称不能为空;
         * categoryId: 分类不能为空
         */
        String message =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .distinct()
                        .collect(
                                Collectors.joining("; ")
                        );


        return ResponseEntity
                .badRequest()
                .body(
                        Result.fail(
                                CommonErrorCode.PARAM_ERROR,
                                message
                        )
                );
    }


    /**
     * 处理普通表单、Query参数绑定异常。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>>
    handleBindException(
            BindException exception
    ) {

        String message =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .distinct()
                        .collect(
                                Collectors.joining("; ")
                        );


        return ResponseEntity
                .badRequest()
                .body(
                        Result.fail(
                                CommonErrorCode.PARAM_ERROR,
                                message
                        )
                );
    }


    /**
     * 处理：
     *
     * @RequestParam
     * @PathVariable
     *
     * 上面的Validation异常。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>>
    handleConstraintViolationException(
            ConstraintViolationException exception
    ) {

        String message =
                exception
                        .getConstraintViolations()
                        .stream()
                        .map(violation ->
                                violation
                                        .getPropertyPath()
                                        + ": "
                                        + violation.getMessage()
                        )
                        .collect(
                                Collectors.joining("; ")
                        );


        return ResponseEntity
                .badRequest()
                .body(
                        Result.fail(
                                CommonErrorCode.PARAM_ERROR,
                                message
                        )
                );
    }


    /**
     * JSON格式错误。
     *
     * 例如前端传：
     *
     * {
     *     "price": "abc"
     * }
     *
     * 但Java要求BigDecimal。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {

        return ResponseEntity
                .badRequest()
                .body(
                        Result.fail(
                                CommonErrorCode.PARAM_ERROR,
                                "请求JSON格式不正确"
                        )
                );
    }


    /**
     * 参数类型转换失败。
     *
     * 例如：
     *
     * /products/abc
     *
     * 但是Controller要求：
     *
     * Long productId
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {

        String message =
                "参数 "
                        + exception.getName()
                        + " 类型不正确";


        return ResponseEntity
                .badRequest()
                .body(
                        Result.fail(
                                CommonErrorCode.PARAM_ERROR,
                                message
                        )
                );
    }


    /**
     * 最后的系统异常兜底。
     *
     * 所有没有被前面捕获的异常都会进入这里。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>>
    handleException(
            Exception exception
    ) {

        /*
         * 详细异常只记录到服务器日志。
         *
         * 绝对不要：
         *
         * return Result.fail(
         *      "500",
         *      exception.getMessage()
         * );
         *
         * 否则可能把：
         *
         * SQL
         * 数据库地址
         * 表结构
         * 文件路径
         *
         * 泄露给前端。
         */
        log.error("系统发生未处理异常", exception);



        return ResponseEntity
                .status(
                        CommonErrorCode
                                .SYSTEM_ERROR
                                .getHttpStatus()
                )
                .body(
                        Result.fail(
                                CommonErrorCode.SYSTEM_ERROR
                        )
                );
    }

}
