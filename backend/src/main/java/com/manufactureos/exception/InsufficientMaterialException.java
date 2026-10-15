package com.manufactureos.exception;

import org.springframework.http.HttpStatus;

/** The requested material quantity is not available to reserve or consume. */
public class InsufficientMaterialException extends ApiException {
    public InsufficientMaterialException(String message) {
        super(HttpStatus.CONFLICT, "INSUFFICIENT_MATERIAL", message);
    }
}
