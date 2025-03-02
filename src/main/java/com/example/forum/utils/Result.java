package com.example.forum.utils;


public class Result<T> {
    private int code; // 状态码
    private String message; // 消息
    private T data; // 数据

    // 私有构造函数，防止外部直接创建实例
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应，不带数据
    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }

    // 成功响应，带数据
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 失败响应
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}