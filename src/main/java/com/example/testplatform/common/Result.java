package com.example.testplatform.common;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    // ---------- 使用枚举的成功响应 ----------
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // ---------- 使用枚举的失败响应 ----------
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setData(null);
        return result;
    }

    // ---------- 自定义错误信息（覆盖枚举默认信息） ----------
    public static <T> Result<T> error(ResultCode resultCode, String customMessage) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(customMessage);
        result.setData(null);
        return result;
    }
}