package com.switchvov.magicgateway;

import com.switchvov.magicrpc.core.api.LoadBalancer;
import com.switchvov.magicrpc.core.api.RegistryCenter;
import com.switchvov.magicrpc.core.meta.InstanceMeta;
import com.switchvov.magicrpc.core.meta.ServiceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * gateway handler.
 *
 * @author switch
 * @since 2024/5/21
 */
@Component
@Slf4j
public class GatewayHandler {

    private final RegistryCenter rc;
    private final LoadBalancer<InstanceMeta> loadBalancer;

    public GatewayHandler(
            RegistryCenter rc,
            LoadBalancer<InstanceMeta> loadBalancer
    ) {
        this.rc = rc;
        this.loadBalancer = loadBalancer;
    }

    Mono<ServerResponse> handle(ServerRequest request) {
        // 1. 通过请求路径或者服务名
        String service = request.path().substring(4);
        ServiceMeta serviceMeta = ServiceMeta.builder().name(service)
                .app("app1").env("dev").namespace("public").build();

        // 2. 通过 rc 拿到所有活着的服务实例
        List<InstanceMeta> instanceMetas = rc.fetchAll(serviceMeta);

        // 3. load balance 获取实例
        InstanceMeta instanceMeta = loadBalancer.choose(instanceMetas);
        String url = instanceMeta.toUrl();

        // 4. 拿到请求的报文
        Mono<String> requestMono = request.bodyToMono(String.class);
        return requestMono.flatMap(requestValue -> invokeFromRegistry(requestValue, url));
    }

    private Mono<ServerResponse> invokeFromRegistry(String requestValue, String url) {
        // 5. 通过webclient发送post请求
        WebClient client = WebClient.create(url);
        Mono<ResponseEntity<String>> entity = client.post()
                .header("Content-Type", "application/json")
                .header("magic.gateway.version", "v1.0.0")
                .bodyValue(requestValue)
                .retrieve()
                .toEntity(String.class);

        // 6. 通过 entity 获取响应报文
        Mono<String> body = entity.mapNotNull(ResponseEntity::getBody);
        body.subscribe(source -> log.info(" ===> [MagicGateway] response: {}", source));

        // 7. 组装响应报文
        return ServerResponse.ok()
                .header("Content-Type", "application/json")
                .header("magic.gateway.version", "v1.0.0")
                .body(body, String.class);
    }
}
