package com.example.login.common.exception;

import com.example.login.common.utils.Result;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import static com.example.login.enums.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 处理自定义业务异常（优先拦截）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 记录告警日志（WARN 级别）
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 2. 处理参数校验异常（@Valid 校验失败）
     * 例如：@NotBlank、@NotNull 等校验不通过
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 提取所有字段错误信息，拼接成一条提示
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.error(PARAM_ERROR.getCode(), message);
    }

    /**
     * 3. 处理 GET 请求参数校验异常（如 @RequestParam 校验失败）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("请求参数校验失败：{}", message);
        return Result.error(PARAM_ERROR.getCode(), message);
    }

    /**
     * 4. 处理 JWT 解析异常（通常在 Filter 中已经拦截，但万一漏掉可兜底）
     */
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleJwtException(JwtException e) {
        log.warn("JWT 验证失败：{}", e.getMessage());
        return Result.error(TOKEN_EXPIRED.getCode(), "Token 无效或已过期");
    }

    /**
     * 5. 处理数据库异常（如 DuplicateKeyException 唯一键冲突）
     */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据库唯一键冲突：{}", e.getMessage());
        // 生产环境不要直接把 SQL 异常信息返回给前端，需转换成业务友好提示
        return Result.error(DATA_QUERY_ERROR.getCode(), "数据已存在，请勿重复提交");
    }

    /**
     * 6. 兜底：处理所有未被捕获的异常（系统未知异常）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        // 打印完整堆栈，供运维排查（生产环境可只记录 ERROR 级别）
        log.error("系统未知异常：", e);
        return Result.error(SYSTEM_ERROR.getCode(), "系统繁忙，请稍后再试");
    }
}