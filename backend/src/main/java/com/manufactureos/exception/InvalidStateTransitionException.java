package com.manufactureos.exception;

import org.springframework.http.HttpStatus;

/** An operation was requested on an entity whose current status does not permit it. */
public class InvalidStateTransitionException extends ApiException {
    public InvalidStateTransitionException(String message) {
        super(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", message);
    }
}
