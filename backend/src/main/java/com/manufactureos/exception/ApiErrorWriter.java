package com.manufactureos.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Writes the standard ApiError JSON shape directly to the servlet response, for the
 *  security entry point / access-denied handler which run outside MVC's own error path. */
@Component
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;

    public ApiErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                       int status, String errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ApiError body = ApiError.of(status, errorCode, message, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
