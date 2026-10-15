package com.manufactureos.exception;

import org.springframework.http.HttpStatus;

/** Generic 409: duplicate records, state that no longer matches an expectation. */
public class ConflictException extends ApiException {
    public ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
