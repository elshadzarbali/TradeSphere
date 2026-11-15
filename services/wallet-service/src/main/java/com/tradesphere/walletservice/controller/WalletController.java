package com.tradesphere.walletservice.controller;

import com.tradesphere.walletservice.dto.TransactionRequest;
import com.tradesphere.walletservice.dto.WalletResponse;
import com.tradesphere.walletservice.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing endpoints for wallet management and financial operations.
 * Acts strictly as an HTTP adapter—delegates all business logic and DTO mapping to the {@link WalletService}.
 */
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Endpoints for wallet provisioning, balance inquiries, deposits, and withdrawals")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{userId}")
    @Operation(summary = "Create a new wallet", description = "Provisions an active wallet with a zero initial balance for the specified user.")
    @ApiResponse(responseCode = "201", description = "Wallet created successfully")
    public ResponseEntity<WalletResponse> createWallet(
            @Parameter(description = "Unique identifier of the user", example = "user-123")
            @PathVariable String userId,
            @Parameter(description = "3-letter ISO currency code", example = "USD")
            @RequestParam(defaultValue = "USD") String currency) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.createWallet(userId, currency));
    }

    @GetMapping("/{userId}")
    @ApiResponse(responseCode = "200", description = "Wallet details retrieved successfully")
    public ResponseEntity<WalletResponse> getWallet(
            @Parameter(description = "Unique identifier of the user", example = "user-123")
            @PathVariable String userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/{userId}/deposit")
    @ApiResponse(responseCode = "200", description = "Deposit processed successfully")
    public ResponseEntity<WalletResponse> deposit(
            @Parameter(description = "Unique identifier of the user", example = "user-123")
            @PathVariable String userId,
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity.ok(walletService.processDeposit(userId, request));
    }

    @PostMapping("/{userId}/withdraw")
    @ApiResponse(responseCode = "200", description = "Withdrawal processed successfully")
    public ResponseEntity<WalletResponse> withdraw(
            @Parameter(description = "Unique identifier of the user", example = "user-123")
            @PathVariable String userId,
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity.ok(walletService.processWithdrawal(userId, request));
    }
}