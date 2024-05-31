package com.switchvov.magicgateway;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * gateway filter.
 *
 * @author switch
 * @since 2024/5/31
 */
public interface GatewayFilter {
    Mono<Void> filter(ServerWebExchange exchange);
}
