package com.buu.auth.exception;

import com.buu.auth.common.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public R<Void> handleAuthException(AuthException exception) {
        return R.fail(401, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception exception) {
        return R.fail(500, "认证服务异常：" + exception.getMessage());
    }
}
