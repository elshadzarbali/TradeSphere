package com.tradesphere.walletservice.exception;

import com.tradesphere.walletservice.exception.model.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class WalletNotFoundException extends BaseDomainException {

    public WalletNotFoundException(String message) {
        super(ErrorCode.WALLET_NOT_FOUND, message);
    }

    public WalletNotFoundException(String message, Map<String, Object> properties) {
        super(ErrorCode.WALLET_NOT_FOUND, message, properties);

    }

    public static WalletNotFoundException forUser(String userId) {
        return new WalletNotFoundException(
                "Wallet not found for user: " + userId, Map.of("userId", userId)
        );
    }

    public static WalletNotFoundException forId(UUID walletId) {
        return new WalletNotFoundException(
                "Wallet not found with ID: " + walletId, Map.of("walletId", walletId)
        );
    }

}
