package com.switchvov.magicgateway.filter;

import com.switchvov.magicgateway.GatewayFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Demo filter.
 *
 * @author switch
 * @since 2024/5/31
 */
@Component("demoFilter")
@Slf4j
public class DemoFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange) {
        log.info(" ===>[MagicGateway] filters: demo filter ...");
        exchange.getRequest().getHeaders().toSingleValueMap()
                .forEach((k, v) -> log.debug("demo filter: header key:{} value:{}", k, v));
        return Mono.empty();
    }
}
