package com.buu.distribution.exception;

import com.buu.distribution.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.failure(400, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.failure(500, "系统异常：" + exception.getMessage());
    }
}
