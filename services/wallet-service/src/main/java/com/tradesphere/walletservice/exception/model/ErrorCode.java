package com.tradesphere.walletservice.exception.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Centralized catalog of domain-specific error codes, titles, and corresponding HTTP statuses.
 * Prevents magic strings across exception classes and enables reliable client-side error handling.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Domain & Business Errors
    WALLET_NOT_FOUND("WALLET_001", "Wallet Not Found", HttpStatus.NOT_FOUND),
    WALLET_ALREADY_EXISTS("WALLET_002", "Wallet Already Exists", HttpStatus.CONFLICT),
    INSUFFICIENT_FUNDS("WALLET_003", "Insufficient Funds", HttpStatus.UNPROCESSABLE_ENTITY),
    DUPLICATE_TRANSACTION("WALLET_004", "Duplicate Transaction", HttpStatus.CONFLICT),
    CONCURRENT_MODIFICATION("WALLET_005", "Concurrent Modification Conflict", HttpStatus.CONFLICT),

    // Request & Validation Errors (400)
    INVALID_ARGUMENT("WALLET_400", "Invalid Argument", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON("WALLET_401", "Malformed JSON Request", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("WALLET_422", "Validation Failed", HttpStatus.BAD_REQUEST),

    // System Errors (500)
    INTERNAL_ERROR("WALLET_500", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String title;
    private final HttpStatus httpStatus;
}
