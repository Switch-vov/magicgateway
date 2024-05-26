package com.switchvov.magicgateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * gateway web filter.
 *
 * @author switch
 * @since 2024/5/26
 */
@Component
@Slf4j
public class GatewayWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info(" ===>[MagicGateway] Magic Gateway web filter ...");
        if (Objects.isNull(exchange.getRequest().getQueryParams().getFirst("mock"))) {
            return chain.filter(exchange);
        }
        String mock = """
                {"result": "mock"}
                """;
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("magic.gateway.version", "v1.0.0");
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(mock.getBytes())));
    }
}
