package com.tradesphere.walletservice.exception;

import com.tradesphere.walletservice.exception.model.ErrorCode;

import java.math.BigDecimal;
import java.util.Map;

public class InsufficientFundsException extends BaseDomainException {

    public InsufficientFundsException(String message) {
        super(ErrorCode.INSUFFICIENT_FUNDS, message);
    }

    public InsufficientFundsException(String message, Map<String, Object> properties) {
        super(ErrorCode.INSUFFICIENT_FUNDS, message, properties);
    }

    public static InsufficientFundsException of(BigDecimal currentBalance, BigDecimal requestedAmount) {
        return new InsufficientFundsException(
                String.format("Insufficient funds. Current balance: %s, Requested: %s", currentBalance, requestedAmount),
                Map.of("currentBalance", currentBalance, "requestedAmount", requestedAmount)
        );
    }

}
