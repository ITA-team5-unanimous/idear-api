package com.idear.backend.global;

import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }
    
    // 404 Not Found 처리
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("handleNoResourceFoundException", e);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // 405, 지원하지 않는 HTTP Method 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }
}
