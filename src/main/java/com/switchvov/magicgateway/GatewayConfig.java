package com.switchvov.magicgateway;


import com.switchvov.magicrpc.core.api.LoadBalancer;
import com.switchvov.magicrpc.core.api.RegistryCenter;
import com.switchvov.magicrpc.core.cluster.RoundRobinLoadBalancer;
import com.switchvov.magicrpc.core.meta.InstanceMeta;
import com.switchvov.magicrpc.core.registry.magic.MagicRegistryCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gateway config.
 *
 * @author switch
 * @since 2024/5/24
 */
@Configuration
public class GatewayConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter rc() {
        return new MagicRegistryCenter();
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRobinLoadBalancer<>();
    }
}
