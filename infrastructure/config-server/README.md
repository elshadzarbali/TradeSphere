# 🛠️ TradeSphere - Config Server

This module is the **Centralized Configuration Server** for the TradeSphere microservices architecture. It acts as the single source of truth for all environment variables, database credentials, and routing configurations across the entire ecosystem.

## 📚 Technical Documentation

To fully understand the architecture, configuration flow, and environment setups, please refer to the detailed engineering guides located in the `docs/` directory:

1. **[Architecture & Flow Overview](docs/config-server-architecture.md)**  
   *Visual Mermaid diagrams explaining how the Config Server interacts with Eureka and other microservices during startup.*
2. **[Environment Strategies (Dev vs. Prod)](docs/config-server-environments.md)**  
   *A guide on how the server operates locally (`native` profile) versus how it secures and scales in a Production environment.*
3. **[Global Configuration Guide](docs/shared-application-yml-guide.md)**  
   *A deep dive into the `shared-configs/application.yml` file, explaining property inheritance and the Database-per-Service pattern.*

## 🚀 Quick Start (Development Mode)

1. Ensure the **Eureka Server** (`eureka-server` module) is running on port `8761`.
2. Start the Config Server by running `ConfigServerApplication.java`.
3. The server will boot up on port `8888` and auto-register with Eureka.
4. Verify it's serving files by visiting the following URL in your browser:
   ```text
   http://localhost:8888/application/default
   ```

## ⚙️ Core Technologies

* **Java 21**
* **Spring Boot 3.x**
* **Spring Cloud Config**
* **Netflix Eureka Client**