package ru.warehouse.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorBody(String error, int status, String timestamp) {}

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorBody> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.status())
                .body(new ErrorBody(ex.getMessage(), ex.status().value(), Instant.now().toString()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage() == null ? "invalid" : f.getDefaultMessage(),
                        (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation failed",
                "status", 400,
                "fields", fields,
                "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorBody> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorBody(ex.getMessage(), 400, Instant.now().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorBody(ex.getMessage(), 400, Instant.now().toString()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorBody> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorBody("Access denied", 403, Instant.now().toString()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorBody> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorBody(ex.getMessage(), 401, Instant.now().toString()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorBody> handleIntegrity(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause().getMessage();
        String friendly;
        if (raw != null && raw.contains("invoice_number")) {
            friendly = "Накладная с таким номером уже существует";
        } else if (raw != null && raw.contains("tracking_number")) {
            friendly = "Поставка с таким трек-номером уже существует";
        } else if (raw != null && raw.contains("sku")) {
            friendly = "Товар с таким SKU уже существует";
        } else if (raw != null && raw.contains("username")) {
            friendly = "Пользователь с таким логином уже существует";
        } else if (raw != null && raw.contains("foreign key")) {
            friendly = "Ссылка на несуществующую запись";
        } else {
            friendly = "Конфликт данных";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorBody(friendly, 409, Instant.now().toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorBody(ex.getMessage() == null ? "Internal error" : ex.getMessage(),
                        500, Instant.now().toString()));
    }
}
