package com.tradesphere.walletservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    // if index fragmentation problem happens, use uuidv7 instead of uuidv4
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // In a microservice architecture, user data belongs to user-service.
    // We only keep the reference (userId) here.
    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WalletStatus status = WalletStatus.ACTIVE;

    // Optimistic Locking: Prevents lost updates when concurrent transactions modify the balance.
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum WalletStatus {
        ACTIVE, BLOCKED, CLOSED
    }
}
