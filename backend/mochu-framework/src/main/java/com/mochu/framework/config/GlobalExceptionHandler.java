package com.mochu.framework.config;

import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.info("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<R.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new R.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("参数校验失败: {}", errors);
        return R.fail(400, "参数校验失败", errors);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        List<R.FieldError> errors = e.getFieldErrors().stream()
                .map(fe -> new R.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return R.fail(400, "参数校验失败", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        List<R.FieldError> errors = e.getConstraintViolations().stream()
                .map(cv -> new R.FieldError(getFieldName(cv), cv.getMessage()))
                .toList();
        return R.fail(400, "参数校验失败", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "系统繁忙，请稍后重试");
    }

    private String getFieldName(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath().toString();
        int dotIndex = path.lastIndexOf('.');
        return dotIndex > 0 ? path.substring(dotIndex + 1) : path;
    }
}
