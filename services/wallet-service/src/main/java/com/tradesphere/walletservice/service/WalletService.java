package com.tradesphere.walletservice.service;

import com.tradesphere.walletservice.domain.entity.Wallet;
import com.tradesphere.walletservice.domain.entity.WalletTransaction;
import com.tradesphere.walletservice.dto.TransactionRequest;
import com.tradesphere.walletservice.dto.WalletResponse;
import com.tradesphere.walletservice.exception.DuplicateTransactionException;
import com.tradesphere.walletservice.exception.InsufficientFundsException;
import com.tradesphere.walletservice.exception.WalletAlreadyExistsException;
import com.tradesphere.walletservice.exception.WalletNotFoundException;
import com.tradesphere.walletservice.mapper.WalletMapper;
import com.tradesphere.walletservice.repository.WalletRepository;
import com.tradesphere.walletservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponse createWallet(String userId, String currency) {
        // TODO: Check existsByUserIdAndCurrency
        if (walletRepository.existsByUserId(userId)) {
            throw WalletAlreadyExistsException.forUser(userId);
        }

        String normalizedCurrency = (currency != null && !currency.isBlank())
                ? currency.trim().toUpperCase() : "USD";

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency(normalizedCurrency)
                .status(Wallet.WalletStatus.ACTIVE)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Created new wallet with ID: {} for user: {}", savedWallet.getId(), userId);

        return walletMapper.toDto(savedWallet);
    }

    // Marked as readOnly to disable Hibernate dirty checking and optimize performance.
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(String userId) {
        // TODO: findByUserId returns List of Wallet, user have USD, AZN and etc currency wallet
        Wallet wallet = walletRepository
                .findByUserId(userId)
                .orElseThrow(() -> WalletNotFoundException.forUser(userId));

        return walletMapper.toDto(wallet);
    }

    @Transactional
    public WalletResponse processDeposit(String userId, TransactionRequest request) {
        BigDecimal amount = request.amount();
        String referenceId = request.referenceId();

        // Defensive programming: Validate amount even if validated at the controller level
        validateTransactionAmount(amount, "Deposit");

        // Idempotency check: Prevent duplicate processing of the same external event (e.g., payment webhook retries)
        if (referenceId != null && transactionRepository.existsByReferenceId(referenceId)) {
            throw DuplicateTransactionException.forReference(referenceId);
        }

        Wallet wallet = walletRepository
                .findByUserId(userId)
                .orElseThrow(() -> WalletNotFoundException.forUser(userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        // Append immutable audit record to the transaction ledger
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type(WalletTransaction.TransactionType.DEPOSIT)
                .amount(amount)
                .referenceId(referenceId)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);

        log.info("Successfully deposited {} {} to wallet ID: {}", amount, savedWallet.getCurrency(), savedWallet.getId());
        return walletMapper.toDto(savedWallet);
    }

    @Transactional
    public WalletResponse processWithdrawal(String userId, TransactionRequest request) {
        BigDecimal amount = request.amount();
        String referenceId = request.referenceId();

        // Defensive programming: Validate withdrawal amount
        validateTransactionAmount(amount, "Withdrawal");

        // Idempotency check: Ensure the same withdrawal request is not executed twice
        if (referenceId != null && transactionRepository.existsByReferenceId(referenceId)) {
            throw DuplicateTransactionException.forReference(referenceId);
        }

        Wallet wallet = walletRepository
                .findByUserId(userId)
                .orElseThrow(() -> WalletNotFoundException.forUser(userId));

        // Domain invariant check: Verify sufficient funds before debiting (throws 422 with balance metadata)
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw InsufficientFundsException.of(wallet.getBalance(), amount);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        // amounts are always stored as positive values; 'type' indicates direction
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(savedWallet)
                .type(WalletTransaction.TransactionType.WITHDRAWAL)
                .amount(amount)
                .referenceId(referenceId)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);

        log.info("Successfully withdrew {} {} from wallet ID: {}", amount, savedWallet.getCurrency(), savedWallet.getId());
        return walletMapper.toDto(savedWallet);
    }

    private void validateTransactionAmount(BigDecimal amount, String operationType) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(operationType + " amount must be greater than zero");
        }
    }
}
