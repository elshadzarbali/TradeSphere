# High Availability (HA) Cluster Configuration

For production environments or local testing of failover scenarios, the Eureka Server should be configured in a peer-aware High Availability (HA) cluster. 

Running a standalone registry creates a **Single Point of Failure (SPOF)**. If the standalone Eureka server goes down, microservices will not be able to discover each other. In an HA cluster, multiple Eureka instances replicate their registry state to one another. If one node fails, the others continue to serve the network seamlessly.

---

## 1. Local Network Configuration (Hostname Mapping)

To simulate a multi-node cluster on a single local machine, you must map custom aliases to your localhost IP. 

Add the following lines to your operating system's hosts file:
- **Windows:** `C:\Windows\System32\drivers\etc\hosts`
- **Mac/Linux:** `/etc/hosts`

```text
127.0.0.1 peer1
127.0.0.1 peer2
```
*Note: You may need Administrator/Sudo privileges to edit this file.*

---

## 2. Application Configuration (`application.yml`)

When running in HA mode, nodes must register with each other and fetch each other's registry. Therefore, `register-with-eureka` and `fetch-registry` must be set to `true`.

Below is the complete multi-profile configuration to simulate two peers (`peer1` and `peer2`):

```yaml
spring:
  application:
    name: eureka-server

---
# ==========================================
# PROFILE: PEER 1
# ==========================================
spring:
  config:
    activate:
      on-profile: peer1
server:
  port: 8761
eureka:
  instance:
    hostname: peer1
  client:
    register-with-eureka: true
    fetch-registry: true
    serviceUrl:
      # Peer 1 replicates its state to Peer 2
      defaultZone: http://peer2:8762/eureka/ 

---
# ==========================================
# PROFILE: PEER 2
# ==========================================
spring:
  config:
    activate:
      on-profile: peer2
server:
  port: 8762
eureka:
  instance:
    hostname: peer2
  client:
    register-with-eureka: true
    fetch-registry: true
    serviceUrl:
      # Peer 2 replicates its state to Peer 1
      defaultZone: http://peer1:8761/eureka/ 
```

---

## 3. How to Run the Cluster in IntelliJ IDEA

To start both nodes simultaneously in IntelliJ IDEA:

1. Open **Run/Debug Configurations** (Edit Configurations...).
2. Duplicate your existing `EurekaServerApplication` configuration.
3. Name the first one: `EurekaServer-Peer1`.
   - Go to **Active Profiles** (or VM options) and set the profile to `peer1`.
   - *(If using VM options: `-Dspring.profiles.active=peer1`)*
4. Name the second one: `EurekaServer-Peer2`.
   - Set the profile to `peer2`.
5. Run both configurations.

*Verification:* Open `http://localhost:8761` and `http://localhost:8762` in your browser. Under the **"DS Replicas"** section of the Eureka dashboard, you should see `peer2` listed in the first one, and `peer1` listed in the second one.

---

## 4. Client Microservice Configuration

When configuring other microservices (e.g., `api-gateway`, `wallet-service`) to connect to this highly available cluster, you must provide the URLs of all peers as a comma-separated list. 

This ensures that if `peer1` is down, the microservice will automatically fall back to `peer2`.

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://peer1:8761/eureka/,http://peer2:8762/eureka/
```