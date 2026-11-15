package com.tradesphere.walletservice.exception.handler;

import com.tradesphere.walletservice.exception.*;
import com.tradesphere.walletservice.exception.model.ApiErrorResponse;
import com.tradesphere.walletservice.exception.model.ErrorCode;
import com.tradesphere.walletservice.exception.model.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Sensitive field names whose rejected values must never be echoed back in responses or logs
    private static final Set<String> SENSITIVE_FIELDS = Set.of("password", "passcode", "pin", "cvv", "secret", "token");

    // Unified handler for all custom business/domain exceptions extending BaseDomainException.
    // Dynamically extracts HTTP status, error code, title, and metadata properties from the exception instance.
    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(BaseDomainException ex, HttpServletRequest request) {
        log.warn(
                "Domain exception [{} - {}] on path {}: {}",
                ex.getErrorCode(), ex.getTitle(), request.getRequestURI(), ex.getMessage()
        );

        ApiErrorResponse response = new ApiErrorResponse(
                ex.getHttpStatus(),
                ex.getErrorCode(),
                ex.getTitle(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getProperties(),
                null
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    // Handles 409 Conflict when Optimistic Locking (@Version) fails due to concurrent updates.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleOptimisticLockingFailure(OptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic locking failure on path {}: Concurrent modification detected", request.getRequestURI());
        ErrorCode code = ErrorCode.CONCURRENT_MODIFICATION;
        return new ApiErrorResponse(
                code.getHttpStatus(),
                code.getCode(),
                code.getTitle(),
                "The wallet balance was modified by another transaction at the same time. Please retry your request.",
                request.getRequestURI(),
                null,
                null
        );
    }

    // Handles @Valid DTO field validation failures.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapToValidationError)
                .toList();

        log.warn("Validation failed on path {} with {} error(s)", request.getRequestURI(), errors.size());
        ErrorCode code = ErrorCode.VALIDATION_FAILED;

        return new ApiErrorResponse(
                code.getHttpStatus(),
                code.getCode(),
                code.getTitle(),
                "One or more fields in the request payload failed validation.",
                request.getRequestURI(),
                null,
                errors
        );
    }

    /**
     * Handles malformed JSON syntax or incompatible field types in the request body (e.g., sending a string for BigDecimal).
     * Intentionally returns a generic message to prevent leaking internal Jackson/Java class names to clients.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorCode code = ErrorCode.MALFORMED_JSON;
        return new ApiErrorResponse(
                code.getHttpStatus(),
                code.getCode(),
                code.getTitle(),
                "The request body is invalid, unreadable, or contains incompatible data types.",
                request.getRequestURI(),
                null,
                null
        );
    }

    // Handles illegal arguments, type mismatches in URL/query parameters, or missing required parameters.
    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidArgument(Exception ex, HttpServletRequest request) {
        log.warn("Invalid argument/parameter on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorCode code = ErrorCode.INVALID_ARGUMENT;

        String detail = ex.getMessage();
        if (ex instanceof MethodArgumentTypeMismatchException mismatchEx) {
            detail = String.format("Parameter '%s' has an invalid value '%s'.",
                    mismatchEx.getName(), mismatchEx.getValue());
        }

        return new ApiErrorResponse(
                code.getHttpStatus(),
                code.getCode(),
                code.getTitle(),
                detail,
                request.getRequestURI(),
                null,
                null
        );
    }

    /**
     * Fallback handler for any unexpected system or database errors (500 Internal Server Error).
     * Logs the full stack trace internally while returning a safe, generic message to the client.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred on path {}", request.getRequestURI(), ex);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return new ApiErrorResponse(
                code.getHttpStatus(),
                code.getCode(),
                code.getTitle(),
                "An unexpected error occurred while processing the request. Please contact support.",
                request.getRequestURI(),
                null,
                null
        );
    }

    private ValidationError mapToValidationError(FieldError fieldError) {
        String fieldName = fieldError.getField();
        Object rejectedValue = fieldError.getRejectedValue();

        if (rejectedValue != null && SENSITIVE_FIELDS.contains(fieldName.toLowerCase())) {
            rejectedValue = "***MASKED***";
        }

        return new ValidationError(
                fieldName,
                rejectedValue,
                fieldError.getDefaultMessage()
        );
    }
}
