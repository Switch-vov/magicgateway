package com.switchvov.magicgateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * hello handler.
 *
 * @author switch
 * @since 2024/5/21
 */
@Component
@Slf4j
public class HelloHandler {
    Mono<ServerResponse> handle(ServerRequest request) {


        String url = "http://localhost:8080/magicrpc";
        String requestJson = """
                {
                  "service": "com.switchvov.magicrpc.demo.api.UserService",
                  "methodSign": "findById@1_int",
                  "args": [100]
                }
                """;

        WebClient client = WebClient.create(url);
        Mono<ResponseEntity<String>> entity = client.post()
                .header("Content-Type", "application/json")
                .bodyValue(requestJson)
                .retrieve()
                .toEntity(String.class);

        Mono<String> body = entity.mapNotNull(ResponseEntity::getBody);
        body.subscribe(source -> log.info(" ===>[MagicGateway] response:{}", source));
        return ServerResponse.ok()
                .header("Content-Type", "application/json")
                .header("magic.gateway.version", "v1.0.0")
                .body(body, String.class);
    }
}
