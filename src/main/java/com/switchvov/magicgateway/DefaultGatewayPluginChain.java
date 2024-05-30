package com.switchvov.magicgateway;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * default gateway plugin chain.
 *
 * @author switch
 * @since 2024/5/30
 */
public class DefaultGatewayPluginChain implements GatewayPluginChain {

    private List<GatewayPlugin> plugins;
    private int index = 0;

    public DefaultGatewayPluginChain(List<GatewayPlugin> plugins) {
        this.plugins = plugins;
    }


    @Override
    public Mono<Void> hander(ServerWebExchange exchange) {
        return Mono.defer(() -> {
            if (index < plugins.size()) {
                return plugins.get(index++).handle(exchange, this);
            }
            return Mono.empty();
        });
    }
}
