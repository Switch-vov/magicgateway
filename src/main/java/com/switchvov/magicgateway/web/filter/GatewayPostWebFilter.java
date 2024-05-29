package com.switchvov.magicgateway.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * gateway post web filter
 *
 * @author switch
 * @since 2024/5/26
 */
@Component
@Slf4j
public class GatewayPostWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).doFinally(s -> {
            log.info(" ===>[MagicGateway] Magic Gateway post web filter ...");
            exchange.getAttributes().forEach((k, v) -> log.debug(" ===>[MagicGateway] post filter k:{} v:{}", k, v));
        });
    }
}
