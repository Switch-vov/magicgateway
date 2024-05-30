package com.switchvov.magicgateway.plugin;

import com.switchvov.magicgateway.AbstractGatewayPlugin;
import com.switchvov.magicgateway.GatewayPluginChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * direct proxy plugin
 *
 * @author switch
 * @since 2024/5/30
 */
@Component("directPlugin")
@Slf4j
public class DirectPlugin extends AbstractGatewayPlugin {
    private static final String NAME = "direct";
    private static final String PREFIX = GATEWAY_PREFIX + "/" + NAME + "/";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mono<Void> doHandle(ServerWebExchange exchange, GatewayPluginChain chain) {
        log.info(" ===>[MagicGateway][DirectPlugin] ...]");
        String backend = exchange.getRequest().getQueryParams().getFirst("backend");
        Flux<DataBuffer> requestBody = exchange.getRequest().getBody();

        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("magic.gateway.version", "v1.0.0");
        headers.add("magic.gateway.plugin", getName());

        if (Objects.isNull(backend) || backend.isEmpty()) {
            return requestBody.flatMap(x -> exchange.getResponse().writeWith(Mono.just(x)))
                    .then(chain.hander(exchange));
        }

        WebClient client = WebClient.create(backend);
        Mono<ResponseEntity<String>> entity = client.post()
                .header("Content-Type", "application/json")
                .header("magic.gateway.version", "v1.0.0")
                .body(requestBody, DataBuffer.class)
                .retrieve()
                .toEntity(String.class);

        // 6. 通过 entity 获取响应报文
        Mono<String> body = entity.mapNotNull(ResponseEntity::getBody);
        // body.subscribe(source -> log.info(" ===> [MagicGateway] response: {}", source));

        // 7. 组装响应报文
        return body.flatMap(x -> exchange.getResponse()
                        .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(x.getBytes()))))
                .then(chain.hander(exchange));
    }

    @Override
    public boolean doSupport(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value().startsWith(PREFIX);
    }
}
