package com.switchvov.magicgateway;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * gateway plugin chain.
 *
 * @author switch
 * @since 2024/5/30
 */
public interface GatewayPluginChain {
    Mono<Void> hander(ServerWebExchange exchange);
}
