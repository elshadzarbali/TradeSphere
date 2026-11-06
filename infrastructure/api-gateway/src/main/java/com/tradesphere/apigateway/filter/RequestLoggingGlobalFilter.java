package com.tradesphere.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Reactive Filter that intercepts ALL incoming requests and outgoing responses.
 * Operates asynchronously on the Netty Event Loop without blocking threads.
 */
@Component
@Slf4j
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // ===================================================================
        // 1. PRE-FILTER PHASE (Executes BEFORE routing to downstream service)
        // ===================================================================
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getRawPath();

        // Store the start time inside the reactive ServerWebExchange attributes (Thread-safe!)
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        log.info("➡️ [PRE-FILTER] Incoming Request: {} {} | Client IP: {}",
                method, path, request.getRemoteAddress());

        // ===================================================================
        // 2. FORWARD TO NEXT FILTER & ATTACH POST-FILTER CALLBACK
        // ===================================================================
        // chain.filter(exchange) is asynchronous (returns Mono<Void>).
        // .then(Mono.fromRunnable(...)) registers a non-blocking callback that runs
        // ONLY AFTER the downstream microservice returns its response!
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // ===============================================================
            // 3. POST-FILTER PHASE (Executes AFTER downstream service responds)
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
        // Defines filter priority in the FilterChain.
        // Ordered.HIGHEST_PRECEDENCE (-2147483648) or -1 ensures this runs FIRST on the way in,
        // and LAST on the way out (accurately measuring total latency).
        return -1;
    }
}
