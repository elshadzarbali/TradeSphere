# Global Configuration: `shared-configs/application.yml`

This document explains the purpose, structure, and hierarchy of the global `application.yml` file located in the `shared-configs` directory of the Config Server.

## 1. The Concept of "Global Rulebook"

In the Spring Cloud Config ecosystem, the name of the file matters immensely.
When the Config Server finds a file named exactly **`application.yml`** (or `application.properties`), it treats it as the **Global Rulebook**.

Whenever *any* microservice (e.g., `wallet-service`, `order-service`) requests its configuration, the Config Server will **always** inject the properties from this global file into the response, before adding the service's specific properties.

**Why is this powerful?**
It strictly enforces the DRY (Don't Repeat Yourself) principle across the entire infrastructure. If 15 microservices share the same Eureka server or the same Kafka broker, you define those connections once here, instead of copying them into 15 different repositories.

## 2. Breakdown of Current Global Properties

Here is the exact explanation of the properties we have defined in our global configuration:

### 2.1. Global Eureka Discovery URL

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

* **The Problem it Solves:** Every microservice needs to know where the Service Registry (Eureka) is located to register itself.
* **The Global Solution:** By placing `defaultZone` here, no microservice needs to know where Eureka is in its own code. They only need to know where the *Config Server* is. The Config Server tells them where Eureka is.
* **Production Shift:** In production, this single line will be updated to point to a cluster (e.g., `http://eureka-1:8761/eureka/,http://eureka-2:8761/eureka/`), and *all* microservices will instantly learn about the new cluster without redeploying them.

### 2.2. IP-Based Network Routing

```yaml
eureka:
  instance:
    prefer-ip-address: true
```

* **The Problem it Solves:** In containerized environments (like Docker or Kubernetes), hostnames can be dynamically generated, unpredictable, or fail to resolve properly across different virtual networks.
* **The Global Solution:** `prefer-ip-address: true` forces microservices to register with Eureka using their actual IP addresses rather than their hostnames. When Service A wants to call Service B, Eureka gives it a reliable IP address, ensuring stable microservice-to-microservice communication.

### 2.3. Custom Global Variables

```yaml
tradesphere:
  global:
    environment: "development"
    message: "Hello from Centralized Config Server!"
```

* **The Purpose:** These are custom, domain-specific properties. They demonstrate that you can broadcast any data globally. For instance, you could use this to globally broadcast a `maintenance-mode: true` flag to temporarily stop all services from processing transactions.

## 3. The Override Hierarchy (Global vs. Specific)

What happens if a global rule conflicts with a specific microservice rule?
Spring Cloud uses a strict **"Specific beats Global"** hierarchy.

Imagine we have two files in `shared-configs`:
1. `application.yml` (Global)
2. `wallet-service.yml` (Specific)

### Scenario 1: Additive (Merging)
* Global has: `eureka.client.serviceUrl...`
* Specific has: `spring.datasource.url...`
* **Result:** The `wallet-service` gets **both**. It connects to Eureka and its database.

### Scenario 2: Overriding (Conflict)
* Global has: `tradesphere.global.environment: "development"`
* Specific has: `tradesphere.global.environment: "testing"`
* **Result:** The `wallet-service` gets `"testing"`. The specific file always wins. This allows you to have standard global rules, but make exceptions for individual services when necessary.

## 4. What is the "Database-per-Service" Pattern?

In monolithic applications, all modules (users, products, orders) share a single, giant database. In microservices, this is a dangerous anti-pattern.

The **Database-per-Service** pattern dictates that **each microservice must have its own isolated database** (or at least its own isolated schema).
* `wallet-service` connects to `wallet_db`.
* `trading-service` connects to `trading_db`.

**Why?**
1. **Decoupling:** If the `wallet-service` team changes their table structure, it will never accidentally break the `trading-service`.
2. **Scaling:** If the Trading system gets huge traffic, you can scale `trading_db` on a powerful server without paying for extra resources for the `wallet_db`.

**How it affects Config Server:**
Because every service has a *different* database, you **must never** put `spring.datasource.url` or database passwords in the global `application.yml`. Database configurations belong strictly in the specific files (like `wallet-service.yml`).

## 5. Best Practices for Global Configurations

1. **Keep it Lean:** Only put properties here that are truly shared by at least 80% of your microservices (e.g., Eureka URLs, Kafka clusters, central Logging levels).
2. **No Databases:** Never put database credentials in the global file (enforcing the Database-per-Service pattern).
3. **Use Profiles:** In production, you can have `application-prod.yml` to define global rules strictly for the production environment.