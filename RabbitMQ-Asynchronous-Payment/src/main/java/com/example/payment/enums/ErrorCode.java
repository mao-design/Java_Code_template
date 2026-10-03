package com.example.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 成功 ====================
    SUCCESS("00000", "成功"),

    // ==================== 用户注册 ====================
    USER_ALREADY_EXIST("A0111", "用户名已存在"),
    PASSWORD_NOT_SAME("A0120", "两次输入的密码不一致"),

    // ==================== 用户登录认证 ====================
    USER_NOT_EXIST("A0201", "用户不存在"),
    ACCOUNT_DISABLED("A0202", "账号已禁用"),
    PASSWORD_ERROR("A0203", "用户名或密码错误"),
    LOGIN_EXPIRED("A0204", "登录已过期"),
    REFRESH_TOKEN_INVALID("A0205", "Refresh Token已失效"),
    TOKEN_INVALID("A0206", "Token非法"),
    TOKEN_EXPIRED("A0207", "Token已过期"),

    // ==================== 权限 ====================
    NO_PERMISSION("A0301", "没有访问权限"),
    NOT_LOGIN("A0302", "用户未登录"),


    // ==================== 参数 ====================
    PARAM_ERROR("A0400", "请求参数错误"),
    PARAM_EMPTY("A0401", "参数不能为空"),
    PARAM_FORMAT_ERROR("A0402", "参数格式错误"),
    PARAM_LENGTH_ERROR("A0403", "参数长度错误"),
    PARAM_RANGE_ERROR("A0404", "参数范围错误"),


    // ==================== 文件 ====================
    FILE_UPLOAD_ERROR("A0500", "文件上传失败"),
    FILE_NOT_EXIST("A0501", "文件不存在"),
    FILE_TYPE_ERROR("A0502", "文件格式错误"),
    FILE_TOO_LARGE("A0503", "文件过大"),
    FILE_PARSE_ERROR("A0504", "文件解析失败"),


    // ==================== 系统 ====================
    SYSTEM_ERROR("B0001", "系统异常"),


    // ==================== 数据库 ====================
    DATABASE_ERROR("B0100", "数据库异常"),
    DATA_QUERY_ERROR("B0101", "数据查询失败"),
    DATA_SAVE_ERROR("B0102", "数据保存失败"),
    DATA_UPDATE_ERROR("B0103", "数据更新失败"),
    DATA_DELETE_ERROR("B0104", "数据删除失败"),
    DATA_NOT_EXIST("B0105", "数据不存在"),


    // ==================== Redis ====================
    REDIS_ERROR("B0200", "Redis服务异常"),
    CACHE_NOT_EXIST("B0201", "缓存数据不存在"),


    // ==================== 业务服务 ====================
    SERVICE_ERROR("B0300", "业务处理异常"),


    // ==================== 第三方 ====================
    THIRD_PARTY_ERROR("C0001", "第三方服务异常"),
    SMS_ERROR("C0100", "短信服务异常"),
    SMS_SEND_ERROR("C0101", "短信发送失败"),
    PAYMENT_ERROR("C0200", "支付服务异常"),
    PAYMENT_FAIL("C0201", "支付失败"),
    FILE_SERVICE_ERROR("C0300", "文件服务异常"),
    THIRD_PARTY_TIMEOUT("C0400", "第三方接口超时");


    private final String code;

    private final String message;
}
