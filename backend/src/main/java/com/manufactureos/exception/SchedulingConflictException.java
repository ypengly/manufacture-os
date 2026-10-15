package com.manufactureos.exception;

import org.springframework.http.HttpStatus;

/** A machine or worker schedule overlaps with an existing active assignment. */
public class SchedulingConflictException extends ApiException {
    public SchedulingConflictException(String message) {
        super(HttpStatus.CONFLICT, "SCHEDULING_CONFLICT", message);
    }
}
