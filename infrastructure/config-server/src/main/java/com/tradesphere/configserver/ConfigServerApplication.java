package com.tradesphere.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer // The core annotation that transforms this standard Spring Boot app into a Spring Cloud Config Server
// @EnableDiscoveryClient // (Optional in Spring Boot 3+: The app auto-registers with Eureka because the client dependency is in pom.xml)
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}