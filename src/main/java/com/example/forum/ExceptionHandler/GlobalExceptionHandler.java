package com.example.forum.ExceptionHandler;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.forum.utils.Result;

@RestControllerAdvice // 标记为全局异常处理器
public class GlobalExceptionHandler {
    private static final Log log = LogFactory.get();

    /**
     * 处理参数校验异常（@Valid 触发的校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验失败: {}", errorMsg);
        return Result.error("参数错误: " + errorMsg);
    }

    /**
     * 处理数据库操作异常（如 SQL 错误）
     */
    @ExceptionHandler(java.sql.SQLException.class)
    public Result<String> handleSQLException(java.sql.SQLException e) {
        log.error("数据库操作失败: {}", e.getMessage());
        return Result.error("数据库错误: " + e.getMessage());
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleGlobalException(Exception e) {
        log.error("系统异常: {}", e.getMessage());
        return Result.error("服务器繁忙，请稍后重试");
    }
}