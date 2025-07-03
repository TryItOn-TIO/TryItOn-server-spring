// com/tryiton/core/common/exception/GlobalExceptionHandler.java

package com.tryiton.core.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: status={}, message={}", e.getStatus(), e.getMessage());
        Map<String, Object> body = Map.of(
            "status", e.getStatus().value(),
            "message", e.getMessage()
        );
        return new ResponseEntity<>(body, e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception e) {
        log.error("Unhandled Exception: ", e);
        Map<String, Object> body = Map.of(
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "message", "서버 내부 오류가 발생했습니다."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}