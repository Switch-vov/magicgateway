package com.switchvov.magicgateway;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * gateway router.
 *
 * @author switch
 * @since 2024/5/21
 */
@Component
public class GatewayRouter {

    private final GatewayHandler gatewayHandler;
    private final HelloHandler helloHandler;

    public GatewayRouter(
            GatewayHandler gatewayHandler,
            HelloHandler helloHandler
    ) {
        this.gatewayHandler = gatewayHandler;
        this.helloHandler = helloHandler;
    }

    @Bean
    public RouterFunction<?> gatewayRouterFunction() {
        return route(GET("/gw").or(POST("/gw/**")), gatewayHandler::handle);
    }

    @Bean
    public RouterFunction<?> helloRouterFunction() {
        return route(GET("/hello"), helloHandler::handle);
    }
}
