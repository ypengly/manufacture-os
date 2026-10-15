package com.manufactureos.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all handled application errors. The {@code errorCode} becomes the
 * "error" field in the standard error response (see GlobalExceptionHandler).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
}
