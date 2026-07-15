package com.example.testplatform.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 商品相关 (4200-4299)
    PRODUCT_NOT_FOUND(4201, "商品不存在"),
    PRODUCT_OFF_SHELF(4202, "商品已下架"),

    // 订单相关 (4300-4399)
    ORDER_NOT_FOUND(4301, "订单不存在"),
    ORDER_STATUS_ERROR(4302, "订单状态异常"),
    ORDER_CANNOT_CANCEL(4303, "该订单不可取消"),

    // 通用错误 (4000-4099)
    BAD_REQUEST(4000, "请求参数错误"),
    USER_NOT_FOUND(4001, "用户不存在"),
    USERNAME_EXISTS(4002, "用户名已存在"),
    PASSWORD_ERROR(4003, "密码错误"),

    // 权限相关 (4100-4199)
    UNAUTHORIZED(4100, "未登录"),
    FORBIDDEN(4101, "无权限访问"),

    // 业务错误 (5000-5099)
    STOCK_INSUFFICIENT(5001, "库存不足"),

    // 系统错误 (9999)
    INTERNAL_ERROR(9999, "系统内部错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}