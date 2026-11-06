# 🚪 TradeSphere - API Gateway

This module is the **Single Entry Point (Edge Server / Reverse Proxy)** for the TradeSphere microservices architecture. Built on **Spring Cloud Gateway (Reactive WebFlux / Netty)**, it handles dynamic routing, client-side load balancing, and global request/response filtering on port `8080`.

## 📚 Technical Documentation

For an in-depth engineering breakdown of the Gateway's internals, configurations, and architectural trade-offs, refer to the guides in the `docs/` directory:

1. **[Architecture, Reactive Netty Engine & LoadBalancer Cache](docs/gateway-architecture-and-reactive-engine.md)**  
   *Explains Blocking Tomcat vs. Non-Blocking Netty Event Loops, Caffeine Cache internals (W-TinyLFU), and the Netty DNS `prefer-ip-address` requirement.*
2. **[Routing, Predicates & Filters Guide](docs/routing-predicates-and-filters-guide.md)**  
   *Covers `discovery.locator` vs. Explicit Routes, `StripPrefix` vs. `RewritePath`, and the `fail-fast` startup principle.*
3. **[Custom Filters (`GlobalFilter`) & Production Readiness](docs/custom-filters-and-production-readiness.md)**  
   *Details Pre/Post filtering in reactive pipelines, why `ServerWebExchange` attributes replace `ThreadLocal`, global Netty timeouts, and Actuator route inspection.*

## 🚀 Quick Start (Startup Order)

Because `fail-fast: true` is enabled, services must be started in strict order:

1. Start **Eureka Server** (`infrastructure/eureka-server`) on port `8761`.
2. Start **Config Server** (`infrastructure/config-server`) on port `8888`.
3. Start **API Gateway** (`ApiGatewayApplication.java`) on port `8080`.

### Verification Endpoints
* **Inspect Active Routes:** `http://localhost:8080/actuator/gateway/routes`
* **Health Check:** `http://localhost:8080/actuator/health`
* **Test Routing to Config Server:** `http://localhost:8080/config/application/default`

## ⚙️ Core Technologies
* **Java 21**
* **Spring Boot 3.5.x**
* **Spring Cloud Gateway Server WebFlux (`2025.0.x`)**
* **Project Reactor & Netty**
* **Netflix Eureka Client & Caffeine Cache**