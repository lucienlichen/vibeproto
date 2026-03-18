package com.vibeproto.common.exception;

import com.vibeproto.common.api.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException exception) {
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public Result<Void> handleBadRequest(Exception exception) {
        return Result.failure(4000, "请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return Result.failure(5000, "系统异常，请稍后重试");
    }
}
