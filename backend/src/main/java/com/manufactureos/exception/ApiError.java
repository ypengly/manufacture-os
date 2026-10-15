package com.manufactureos.exception;

import java.time.Instant;

/** Shape of every error response in the API, matching the project spec's error format. */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path);
    }
}
