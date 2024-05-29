package com.switchvov.magicgateway.web.handler;

import com.switchvov.magicrpc.core.api.LoadBalancer;
import com.switchvov.magicrpc.core.api.RegistryCenter;
import com.switchvov.magicrpc.core.meta.InstanceMeta;
import com.switchvov.magicrpc.core.meta.ServiceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * gateway web handler.
 *
 * @author switch
 * @since 2024/5/26
 */
@Component("gatewayWebHandler")
@Slf4j
public class GatewayWebHandler implements WebHandler {
    private final RegistryCenter rc;
    private final LoadBalancer<InstanceMeta> loadBalancer;

    public GatewayWebHandler(
            RegistryCenter rc,
            LoadBalancer<InstanceMeta> loadBalancer
    ) {
        this.rc = rc;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        log.info(" ===>[MagicGateway] Magic Gateway web handler ...");


        // 1. 通过请求路径或者服务名
        String service = exchange.getRequest().getPath().value().substring(4);
        ServiceMeta serviceMeta = ServiceMeta.builder().name(service)
                .app("app1").env("dev").namespace("public").build();

        // 2. 通过 rc 拿到所有活着的服务实例
        List<InstanceMeta> instanceMetas = rc.fetchAll(serviceMeta);

        // 3. load balance 获取实例
        InstanceMeta instanceMeta = loadBalancer.choose(instanceMetas);
        String url = instanceMeta.toUrl();

        // 4. 拿到请求的报文
        Flux<DataBuffer> requestBody = exchange.getRequest().getBody();

        // 5. 通过webclient发送post请求
        WebClient client = WebClient.create(url);
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
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("magic.gateway.version", "v1.0.0");
        return body.flatMap(x -> exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(x.getBytes()))));
    }

}
