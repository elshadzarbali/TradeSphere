package com.tradesphere.walletservice.exception.model;

/**
 * Represents a specific field validation failure within a request payload.
 * Includes the rejected value (sanitized for sensitive fields) to assist client debugging.
 */
public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {}
