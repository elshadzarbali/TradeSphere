package com.tradesphere.walletservice.exception;

import com.tradesphere.walletservice.exception.model.ErrorCode;

import java.util.Map;

public class WalletAlreadyExistsException extends BaseDomainException {

    public WalletAlreadyExistsException(String message) {
        super(ErrorCode.WALLET_ALREADY_EXISTS, message);
    }

    public WalletAlreadyExistsException(String message, Map<String, Object> properties) {
        super(ErrorCode.WALLET_ALREADY_EXISTS, message, properties);
    }

    public static WalletAlreadyExistsException forUser(String userId) {
        return new WalletAlreadyExistsException(
                "Wallet already exists for user: " + userId,
                Map.of("userId", userId)
        );
    }

}
