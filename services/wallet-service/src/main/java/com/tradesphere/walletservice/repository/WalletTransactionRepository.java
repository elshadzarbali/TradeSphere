package com.tradesphere.walletservice.repository;

import com.tradesphere.walletservice.domain.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    /**
     * Retrieves a paginated list of transactions associated with a specific wallet,
     * ordered by creation timestamp in descending order (newest transactions first).
     *
     * Spring Data JPA automatically derives the query:
     * {@code SELECT * FROM wallet_transactions WHERE wallet_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?}
     *
     * @param walletId The unique identifier of the wallet.
     * @param pageable Pagination and sorting configuration parameters.
     * @return A {@link Page} slice of transactions matching the criteria.
     */
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    /**
     * Efficiently checks whether a transaction with the given external reference ID already exists.
     * Used for enforcing **Idempotency** and preventing duplicate execution of financial operations
     * (e.g., handling duplicate payment gateway webhook deliveries).
     *
     * @param referenceId The unique external reference identifier (e.g., Stripe payment ID or order reference).
     * @return true if a transaction with this reference ID exists, false otherwise.
     */
    boolean existsByReferenceId(String referenceId);
}