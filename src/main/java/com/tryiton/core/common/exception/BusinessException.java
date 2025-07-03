package com.tryiton.core.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private HttpStatus status;
    private String message;

    public BusinessException(HttpStatus status, String message)
    {
        super(message);
        this.message = message;
        this.status = status;
    }
}