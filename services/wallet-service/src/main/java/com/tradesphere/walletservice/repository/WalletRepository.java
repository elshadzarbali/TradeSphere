package com.tradesphere.walletservice.repository;

import com.tradesphere.walletservice.domain.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Note: The @Repository annotation is intentionally omitted here because Spring Data JPA
 * automatically detects and registers all JpaRepository sub-interfaces as beans.
 */
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(String userId);

    boolean existsByUserId(String userId);
}