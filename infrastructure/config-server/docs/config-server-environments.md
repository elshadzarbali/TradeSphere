# Config Server: Development vs. Production Configurations

This document provides a deep dive into the `application.yml` configurations for the Config Server. It explains the purpose of each property, how they are set up for local development, and what the industry-standard alternatives are for a Production environment.

---

## 1. Storage Backend: `spring.profiles.active`

The Config Server needs a place to read the configuration files from. This is known as the "Storage Backend".

### 🟢 Development Setting: `native`
```yaml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/shared-configs/
```
* **What it means:** The `native` profile disables the default Git backend. It tells the server to look for configurations in the local file system (specifically inside the `src/main/resources/shared-configs/` folder).
* **Why we use it in Dev:** It allows for **Rapid Prototyping**. If you change a port or a database password locally, you just hit save in your IDE, and the Config Server immediately serves the new data. No need to `git commit` and `git push` for every minor tweak.

### 🔴 Production Alternative: Git Backend (Default)
In production, the `native` profile is entirely removed. By default, Spring Cloud Config expects a Git repository.
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/TradeSphere/tradesphere-configs.git
          username: ${GIT_USERNAME}
          password: ${GIT_PASSWORD}
          default-label: main
```
* **Why we use it in Prod:**
    * **Audit & Version Control:** Every configuration change is tracked. If a bad config breaks the system, you can instantly `git revert`.
    * **Security:** Configuration files are separated from the application code into a secure, private repository.
    * **Webhooks:** You can configure GitHub/GitLab to send a webhook to the Config Server whenever a file is pushed, triggering a dynamic refresh across all microservices automatically.

---

## 2. Service Registration: `eureka.client.serviceUrl.defaultZone`

This property tells the Config Server where the Service Registry (Eureka) is located.

### 🟢 Development Setting: Single Node
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```
* **What it means:** It points to a single, locally running instance of Eureka on port `8761`. If this process dies, discovery fails.

### 🔴 Production Alternative: Highly Available (HA) Cluster
In production, running a single Eureka server is a single point of failure (SPOF). We run multiple Eureka nodes (Peer Awareness).
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-node1.tradesphere.internal:8761/eureka/,http://eureka-node2.tradesphere.internal:8762/eureka/
```
* **What it means:** The Config Server registers itself with multiple Eureka instances. If `node1` goes down, the Config Server seamlessly continues communicating with `node2`. In modern cloud setups (like Kubernetes), this is often replaced by a single internal DNS name that load-balances behind the scenes.

---

## 3. Server Port: `server.port`

### 🟢 Development Setting: `8888`
```yaml
server:
  port: 8888
```
* **What it means:** `8888` is the industry standard default port for Spring Cloud Config.

### 🔴 Production Alternative: Dynamic/Containerized Ports
While you can keep it as `8888` in production, modern deployments (Docker/Kubernetes) often handle ports differently.
```yaml
server:
  port: ${PORT:8888}
```
* **What it means:** This uses environment variable overriding. The server will try to read the `PORT` environment variable injected by the cloud provider (e.g., AWS, Heroku, or Kubernetes). If the variable is not found, it gracefully falls back to `8888`.

---

## 4. Summary: Environment Architecture

| Feature              | Development (Local)                 | Production (Cloud)                                        |
|:---------------------|:------------------------------------|:----------------------------------------------------------|
| **Config Storage**   | Local File System (`native`)        | Private Git Repository (GitHub/GitLab)                    |
| **Update Mechanism** | Save file in IDE -> Restart/Refresh | Git Push -> Webhook -> Auto-Refresh                       |
| **Eureka Discovery** | Single `localhost` node             | Multiple HA Nodes (Cluster)                               |
| **Port Management**  | Static (`8888`)                     | Environment Injected (`${PORT:8888}`)                     |
| **Security**         | Plain text configurations           | Encrypted values (`{cipher}my-secret`) or HashiCorp Vault |
