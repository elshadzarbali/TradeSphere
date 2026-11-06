# 🚪 TradeSphere - API Gateway

This module is the **Single Entry Point (Edge Server / Reverse Proxy)** for the TradeSphere microservices ecosystem. It runs on **Spring Cloud Gateway (Reactive WebFlux / Netty)** on port `8080`.

All external client requests (Frontend/Mobile) enter through port `8080`. The Gateway logs the request, looks up the target microservice's dynamic IP address from Eureka (cached in Caffeine Cache), balances the load across instances, and forwards the traffic non-blockingly.

## 📚 Detailed Engineering Guides (`docs/`)

We have documented every architectural decision, configuration property, and real-world troubleshooting scenario in clear, easy-to-read English inside the `docs/` folder:

1. **[Architecture, Reactive Netty Engine & Caffeine Cache](docs/gateway-architecture-and-reactive-engine.md)**  
   *Explains why we need a Gateway, Blocking Tomcat vs. Non-Blocking Netty (Restaurant Waiter analogy), Spring Cloud 2025+ WebFlux vs. MVC split, Client-Side Load Balancing with Caffeine Cache (`ConcurrentHashMap` lock contention), and how `prefer-ip-address: true` fixes the Windows Netty DNS `NXDOMAIN` error.*
2. **[Routing Rules, Predicates & Filters Guide](docs/routing-predicates-and-filters-guide.md)**  
   *Covers the exact 3-step startup sequence, why `fail-fast: true` replaces `optional:configserver:`, why `discovery.locator.enabled: true` creates duplicate routes in development (and why it must be `false` in production), and when to use `StripPrefix=1` vs. direct path forwarding vs. `RewritePath`.*
3. **[Custom Java Filters (`GlobalFilter`), Timeouts & Retry Logic](docs/custom-filters-and-production-readiness.md)**  
   *Explains `GlobalFilter` vs. `GatewayFilter`, how `RequestLoggingGlobalFilter` measures latency, why `exchange.getAttributes()` must be used instead of `ThreadLocal` in Netty, global timeouts, how Retry skips dead cached IPs, and why Main class annotations like `@EnableDiscoveryClient` are no longer required.*

## 🚀 How to Run (Strict Startup Order)

Because `fail-fast: true` is active, always start the modules in this exact order:

1. **Discovery Server (`infrastructure/eureka-server`)** — Port `8761`
2. **Config Server (`infrastructure/config-server`)** — Port `8888`
3. **API Gateway (`infrastructure/api-gateway`)** — Port `8080`

### Verification URLs
* **Inspect Active Routes in Memory:** `http://localhost:8080/actuator/gateway/routes`
* **Gateway Health Status:** `http://localhost:8080/actuator/health`
* **Test Routing & `StripPrefix=1` via Config Server:** `http://localhost:8080/config/application/default`

## ⚙️ Tech Stack
* **Java 21 & Spring Boot 3.5.x**
* **Spring Cloud Gateway Server WebFlux (`2025.0.x`)**
* **Project Reactor & Netty (Non-Blocking Event Loop)**
* **Netflix Eureka Client & Spring Cloud LoadBalancer**
* **Caffeine Cache (W-TinyLFU In-Memory Caching)**