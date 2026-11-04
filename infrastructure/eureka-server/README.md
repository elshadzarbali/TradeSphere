# Eureka Server (Service Discovery)

This module acts as the central Service Registry for the **TradeSphere** microservices architecture. All services in the ecosystem register themselves here to discover and communicate with each other dynamically.

## Current Mode: Standalone
By default, this server runs in standalone mode for local development.
- **Port:** `8761`
- **Register with Eureka:** `false`
- **Fetch Registry:** `false`

---

## 📚 Documentation & Advanced Configurations
For advanced setups and architectural guidelines, please refer to the specific documentation files below:

- [High Availability (HA) Cluster Setup](docs/eureka-ha-cluster-setup.md) - *Guide to configuring multiple Eureka instances for failover and replication.*
- *(More guides will be added here in the future...)*