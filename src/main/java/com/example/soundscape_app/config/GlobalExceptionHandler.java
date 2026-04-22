package com.spotify.config;

import com.spotify.dto.response.global.GlobalErrorResponse;
import com.spotify.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Helpers ---
    private ResponseEntity<GlobalErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new GlobalErrorResponse(message, status.value()));
    }

    private String firstFieldError(BindingResult result) {
        StringBuilder sb = new StringBuilder("Validation failed: ");
        for (FieldError e : result.getFieldErrors()) {
            sb.append(e.getField()).append(" - ").append(e.getDefaultMessage()).append("; ");
        }
        return sb.toString();
    }

    // --- Explicit mappings (auth/security/oauth/registration) ---
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GlobalErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, ex.getReason());
    }

    @ExceptionHandler({UnauthorizedAccessException.class, TokenBlacklistedException.class, InvalidTokenException.class})
    public ResponseEntity<GlobalErrorResponse> handleUnauthorizedLike(RuntimeException ex) {
        // Vì FE cần 401 rõ ràng cho các trường hợp không hợp lệ/đã bị thu hồi
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GlobalErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage());
    }

    // --- Registration / input / validation ---
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<GlobalErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex) {
        return build(HttpStatus.BAD_REQUEST, firstFieldError(ex.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed: " + ex.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<GlobalErrorResponse> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad request: " + ex.getMessage());
    }

    // --- Files / payload / database ---
    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<GlobalErrorResponse> handleInvalidFile(InvalidFileTypeException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid file type: " + ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the maximum allowed size");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, "Data integrity violation: " + ex.getMostSpecificCause().getMessage());
    }

    // --- Google / IO ---
    @ExceptionHandler(GeneralSecurityException.class)
    public ResponseEntity<GlobalErrorResponse> handleSecurity(GeneralSecurityException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Security error: " + ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<GlobalErrorResponse> handleIO(IOException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "File/IO error: " + ex.getMessage());
    }

    // --- Runtime / fallback ---
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalErrorResponse> handleRuntime(RuntimeException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof IOException io) return handleIO(io); // giữ nguyên IO 500
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Runtime error: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleAny(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + ex.getMessage());
    }
}
