package com.tradesphere.walletservice.exception;

import com.tradesphere.walletservice.exception.model.ErrorCode;

import java.util.Map;

public class DuplicateTransactionException extends BaseDomainException {

    public DuplicateTransactionException(String message) {
        super(ErrorCode.DUPLICATE_TRANSACTION, message);
    }

    public DuplicateTransactionException(String message, Map<String, Object> properties) {
        super(ErrorCode.DUPLICATE_TRANSACTION, message, properties);
    }

    public static DuplicateTransactionException forReference(String referenceId) {
        return new DuplicateTransactionException(
                "Duplicate transaction reference: " + referenceId,
                Map.of("referenceId", referenceId)
        );
    }

}
