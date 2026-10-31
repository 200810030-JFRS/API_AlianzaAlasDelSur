package com.alasur.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends RuntimeException {
    private final HttpStatus status;

    public ReglaNegocioException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
