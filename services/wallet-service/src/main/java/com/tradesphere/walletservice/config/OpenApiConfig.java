package com.tradesphere.walletservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI 3.0 / Swagger UI documentation.
 * Defines global API metadata and server URLs (Direct & API Gateway).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI walletServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradeSphere - Wallet Service API")
                        .description("Enterprise RESTful API for managing user wallets, balances, and immutable ledger transactions.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("TradeSphere Engineering")
                                .email("dev@tradesphere.com")
                        )
                )
                .servers(
                        List.of(
                                new Server().url("http://localhost:8081").description("Direct Local Wallet Service"),
                                new Server().url("http://localhost:8080").description("API Gateway")
                        )
                );
    }
}
