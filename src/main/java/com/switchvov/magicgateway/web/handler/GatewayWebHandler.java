package com.switchvov.magicgateway.web.handler;

import com.switchvov.magicgateway.DefaultGatewayPluginChain;
import com.switchvov.magicgateway.GatewayFilter;
import com.switchvov.magicgateway.GatewayPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * gateway web handler.
 *
 * @author switch
 * @since 2024/5/26
 */
@Component("gatewayWebHandler")
@Slf4j
public class GatewayWebHandler implements WebHandler {

    private final List<GatewayPlugin> plugins;

    private final List<GatewayFilter> filters;

    public GatewayWebHandler(
            List<GatewayPlugin> plugins,
            List<GatewayFilter> filters
    ) {
        this.plugins = plugins;
        this.filters = filters;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        log.info(" ===>[MagicGateway] Magic Gateway web handler ...");

        if (Objects.isNull(plugins) || plugins.isEmpty()) {
            String mock = """
                    {"result":"no plugin"}
                    """;
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(mock.getBytes())));
        }
        for (GatewayFilter filter : filters) {
            filter.filter(exchange);
        }
        return new DefaultGatewayPluginChain(plugins).hander(exchange);
    }
}
