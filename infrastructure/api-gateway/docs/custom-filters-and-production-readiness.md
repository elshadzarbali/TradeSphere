# Custom Java Filters, Concurrency Rules, Timeouts & Retries

This document explains how custom Java filters work in Spring Cloud Gateway, why reactive concurrency rules matter, and how we protect the system from slow or dead microservices.

---

## 1. `GlobalFilter` vs. `GatewayFilter`: Which One Should We Write?

When you want to write custom Java logic inside the API Gateway (like logging, security checks, or adding custom headers), Spring Cloud Gateway gives you two choices:

| Feature | `GlobalFilter` (What we wrote) | `GatewayFilter` (`AbstractGatewayFilterFactory`) |
| :--- | :--- | :--- |
| **Where does it run?** | Runs automatically on **ALL routes** and **ALL requests**. | Runs **ONLY** on specific routes where you write its name in the YAML `filters:` list. |
| **How to register?** | Just create a class implementing `GlobalFilter, Ordered` and put `@Component` on it. | Create a class extending `AbstractGatewayFilterFactory` and add its name to `api-gateway.yml`. |
| **When to use it?** | Request/Response Logging, JWT Token Validation, Rate Limiting, Distributed Tracing. | Route-specific logic (e.g., checking if the stock market is open *only* for `trading-service-route`). |

---

## 2. Step-by-Step Breakdown of `RequestLoggingGlobalFilter.java`

In `com.tradesphere.apigateway.filter.RequestLoggingGlobalFilter`, we intercept every request to log details and measure how many milliseconds the downstream microservice took to respond:

```java
@Component
@Slf4j
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ===================================================================
        // 1. PRE-FILTER: Runs BEFORE the request is sent to the microservice
        // ===================================================================
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getRawPath();

        // Save current timestamp inside the request's exchange attributes
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        log.info("➡️ [PRE-FILTER] Incoming Request: {} {} | Client IP: {}",
                method, path, request.getRemoteAddress());

        // ===================================================================
        // 2. FORWARD REQUEST & REGISTER POST-FILTER CALLBACK
        // ===================================================================
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // ===============================================================
            // 3. POST-FILTER: Runs AFTER the microservice returns a response
            // ===============================================================
            Long startTime = exchange.getAttribute(START_TIME_ATTR);
            long durationMs = (startTime != null) ? (System.currentTimeMillis() - startTime) : -1;

            HttpStatusCode statusCode = exchange.getResponse().getStatusCode();

            log.info("⬅️ [POST-FILTER] Outgoing Response: {} {} | Status: {} | Latency: {} ms",
                    method, path, statusCode, durationMs);
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
```

### Deep Dive: 3 Critical Questions About This Code

#### Q1: Why do we use `ServerWebExchange` instead of `HttpServletRequest`?
Because `HttpServletRequest` belongs to **Tomcat (Blocking Servlet API)**. Since our Gateway runs on **Netty (Reactive WebFlux)**, it uses `ServerWebExchange`, which holds both the reactive Request (`ServerHttpRequest`) and Response (`ServerHttpResponse`).

#### Q2: Why did we save `startTime` in `exchange.getAttributes()` instead of `ThreadLocal`?
This is one of the most important interview questions for reactive Java developers!
* In classic **Tomcat**, 1 thread handles a request from start to finish. If Thread #1 puts data into `ThreadLocal`, Thread #1 is guaranteed to still be there when the response comes back.
* In **Netty**, threads do **not** wait!
  * Thread `reactor-http-nio-1` might run the **Pre-Filter** for User A, send the request to `wallet-service`, and immediately go serve User B.
  * When `wallet-service` responds 100ms later, a completely different thread (e.g., `reactor-http-nio-3`) might run the **Post-Filter** inside `.then(Mono.fromRunnable(...))`!
* If we used `ThreadLocal`, `nio-3` would not find the `startTime` saved by `nio-1` (or worse, it would read another user's data!).
* `exchange.getAttributes()` is a thread-safe Map attached directly to **that specific HTTP request object**, so whichever thread finishes the request can read the exact `startTime`.

#### Q3: What does `getOrder() { return -1; }` do?
Filters work like layers of an onion (**Onion Architecture**):
* Smaller numbers (`-1`) sit on the **outside** of the onion. Larger numbers (`0`, `1`, `2`) sit on the **inside**.
* Because our logging filter has order `-1`, it is the **first** filter to run when a request arrives (`Pre-Filter`), and the **last** filter to finish when the response leaves (`Post-Filter`). This guarantees that our `Latency: xxx ms` measurement captures the true total time!

---

## 3. Protecting the Gateway: Timeouts & Handling Stale Cache IPs

### 3.1. Global Netty HTTP Timeouts
What happens if `trading-service` has a database bug and freezes for 2 minutes? We don't want connections hanging open in the Gateway forever. In `shared-configs/api-gateway.yml`, we configure:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          httpclient:
            connect-timeout: 3000 # Max 3 seconds (3000ms) to establish TCP connection
            response-timeout: 5s  # Max 5 seconds to wait for HTTP response
```
* If a microservice is completely dead/unreachable, `connect-timeout` fails after 3 seconds.
* If a microservice accepts the connection but takes more than 5 seconds to respond, the Gateway cuts the connection and returns `504 Gateway Timeout`.

---

### 3.2. What Happens When Caffeine Cache Has an Old (Dead) IP? (Retry Mechanism)

Earlier we asked a great architectural question: *"Caffeine Cache keeps service IPs in memory for 35 seconds, and even serves slightly old data during refreshes. What if Instance #1 (`10.0.0.1:8081`) crashes at second 10? Won't the Gateway send requests to a dead IP for the next 25 seconds?"*

Yes, the LoadBalancer will try `10.0.0.1:8081` first because it is still in the cache. However, we protect the user so they never see an error using two mechanisms:

1. **Graceful Shutdown (Normal Stops):** When a service is stopped normally (e.g., during deployment), it stops accepting new work after telling Eureka, and finishes its current requests cleanly.
2. **Gateway Retry Filter (Sudden Crashes):** If a server suddenly loses power, Netty gets a `ConnectException` in 1 millisecond when trying `10.0.0.1:8081`. When we configure the **Retry Filter**, the Gateway catches that error, **skips the dead IP**, asks the LoadBalancer for the **next healthy instance (`10.0.0.2:8082`)**, and sends the request there automatically!

Here is how the Retry Filter is configured in `api-gateway.yml` when needed:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          default-filters:
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT
                methods: GET, OPTIONS, HEAD # ONLY retry safe read-only methods!
                exceptions: java.io.IOException, java.util.concurrent.TimeoutException
                backoff:
                  firstBackoff: 50ms
                  maxBackoff: 500ms
                  factor: 2
```

> **⚠️ Critical FinTech Rule (Why `methods` only has `GET` and NOT `POST`):**
> Never automatically retry `POST` requests (like buying stock or deducting wallet balance) at the Gateway level without an Idempotency Key!
> **Why?** Imagine the Gateway sends `POST /deduct-100-usd` to Instance #1. Instance #1 deducts the $100 from PostgreSQL, but 1 millisecond before sending the `"OK"` response, the network lags and times out after 5 seconds. If the Gateway automatically retries that `POST` request on Instance #2, the user will lose **$200 instead of $100**! Therefore, automatic retries are strictly limited to safe `GET` requests.

---

## 4. Why Do Microservices Use `ApiGatewayApplication` Without Extra Annotations?

You might notice that our main classes look different across modules:
* **`DiscoveryServerApplication`:** Has `@SpringBootApplication` + **`@EnableEurekaServer`** (Mandatory to turn on the Registry server).
* **`ConfigServerApplication`:** Has `@SpringBootApplication` + **`@EnableConfigServer`** (Mandatory to turn on the Config server).
* **`ApiGatewayApplication`:** Only has **`@SpringBootApplication`**!
  * **Why no `@EnableEurekaClient`?** Removed in Spring Cloud 2022+.
  * **Why no `@EnableDiscoveryClient`?** Optional in Spring Boot 3+. Simply having `spring-cloud-starter-netflix-eureka-client` in `pom.xml` triggers Spring Boot **Auto-Configuration** to register with Eureka automatically!
  * **Why no `@EnableGateway`?** Does not exist. Having `spring-cloud-starter-gateway-server-webflux` in `pom.xml` automatically activates the Netty Gateway engine.