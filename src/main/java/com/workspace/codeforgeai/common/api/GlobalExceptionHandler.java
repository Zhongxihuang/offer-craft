package com.workspace.codeforgeai.common.api;

import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final LocalizedMessages localizedMessages;

    public GlobalExceptionHandler(LocalizedMessages localizedMessages) {
        this.localizedMessages = localizedMessages;
    }

    @ExceptionHandler(ApiValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleApiValidationError(ApiValidationException exception,
                                                                     HttpServletRequest request) {
        ApiErrorResponse response = errorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                exception.details()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException exception,
                                                                  HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .toList();

        ApiErrorResponse response = errorResponse(
                HttpStatus.BAD_REQUEST,
                localizedMessages.get("errors.request.validation"),
                request.getRequestURI(),
                details
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                                 HttpServletRequest request) {
        ApiErrorResponse response = errorResponse(
                HttpStatus.BAD_REQUEST,
                localizedMessages.get("errors.request.body.invalid"),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus safeStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        ApiErrorResponse response = errorResponse(
                safeStatus,
                exception.getReason() == null ? safeStatus.getReasonPhrase() : exception.getReason(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(safeStatus).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedError(Exception exception,
                                                                  HttpServletRequest request) {
        log.error("Unhandled request failure", exception);
        ApiErrorResponse response = errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                localizedMessages.get("errors.request.unexpected"),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ApiErrorDetail toDetail(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() == null
                ? localizedMessages.get("errors.request.invalidValue")
                : fieldError.getDefaultMessage();
        return new ApiErrorDetail(fieldError.getField(), message);
    }

    private ApiErrorResponse errorResponse(HttpStatus status,
                                           String message,
                                           String path,
                                           List<ApiErrorDetail> details) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
    }
}
