package ru.warehouse.api.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() { return status; }

    public static ApiException notFound(String msg)      { return new ApiException(HttpStatus.NOT_FOUND, msg); }
    public static ApiException badRequest(String msg)    { return new ApiException(HttpStatus.BAD_REQUEST, msg); }
    public static ApiException unauthorized(String msg)  { return new ApiException(HttpStatus.UNAUTHORIZED, msg); }
    public static ApiException forbidden(String msg)     { return new ApiException(HttpStatus.FORBIDDEN, msg); }
}
