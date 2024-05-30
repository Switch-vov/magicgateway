package com.switchvov.magicgateway.plugin;

import com.switchvov.magicgateway.AbstractGatewayPlugin;
import com.switchvov.magicgateway.GatewayPluginChain;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * magic rpc gateway plugin.
 *
 * @author switch
 * @since 2024/5/30
 */
@Component("magicrpcPlugin")
@Slf4j
public class MagicRpcPlugin extends AbstractGatewayPlugin {
    private static final String NAME = "magicrpc";
    private static final String PREFIX = GATEWAY_PREFIX + "/" + NAME + "/";

    private final RegistryCenter rc;
    private final LoadBalancer<InstanceMeta> loadBalancer;

    public MagicRpcPlugin(
            RegistryCenter rc,
            LoadBalancer<InstanceMeta> loadBalancer
    ) {
        this.rc = rc;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Mono<Void> doHandle(ServerWebExchange exchange, GatewayPluginChain chain) {
        log.info(" ===>[MagicGateway][MagicRpcPlugin] ...]");

        // 1. 通过请求路径或者服务名
        String service = exchange.getRequest().getPath().value().substring(PREFIX.length());
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
        headers.add("magic.gateway.plugin", getName());
        return body.flatMap(x -> exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(x.getBytes()))))
                .then(chain.hander(exchange));
    }

    @Override
    public boolean doSupport(ServerWebExchange exchange) {
        return exchange.getRequest().getPath().value().startsWith(PREFIX);
    }
}
