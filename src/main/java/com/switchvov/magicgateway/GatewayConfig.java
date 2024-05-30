package com.switchvov.magicgateway;


import com.switchvov.magicrpc.core.api.LoadBalancer;
import com.switchvov.magicrpc.core.api.RegistryCenter;
import com.switchvov.magicrpc.core.cluster.RoundRobinLoadBalancer;
import com.switchvov.magicrpc.core.meta.InstanceMeta;
import com.switchvov.magicrpc.core.registry.magic.MagicRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Properties;

import static com.switchvov.magicgateway.GatewayPlugin.*;

/**
 * gateway config.
 *
 * @author switch
 * @since 2024/5/24
 */
@Configuration
@Slf4j
public class GatewayConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter rc() {
        return new MagicRegistryCenter();
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRobinLoadBalancer<>();
    }

    @Bean
    public ApplicationRunner runner(
            @Autowired ApplicationContext applicationContext
    ) {
        return args -> {
            SimpleUrlHandlerMapping handlerMapping = applicationContext.getBean(SimpleUrlHandlerMapping.class);
            Properties mappings = new Properties();
            mappings.put(GATEWAY_PREFIX + "/**", "gatewayWebHandler");
            handlerMapping.setMappings(mappings);
            handlerMapping.initApplicationContext();
            log.info(" ===>[MagicGateway] magic gateway start");
        };
    }
}
