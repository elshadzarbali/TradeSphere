package com.tradesphere.walletservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Entry point for the Wallet Service.
 * Manages user fiat/crypto balances, account ledgers, and transaction histories.
 */
@SpringBootApplication
@EnableDiscoveryClient // (Optional for code reading) Explicitly signals that this service registers with Discovery Server
public class WalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}