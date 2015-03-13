package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
public class ConsulDiscoveryClientConfiguration {

    @Autowired
    private ConsulClient consulClient;

    @Bean
    public ConsulLifecycle consulLifecycle() {
        return new ConsulLifecycle();
    }

    @Bean
    public TtlScheduler ttlScheduler() {
        return new TtlScheduler(heartbeatProperties(), consulClient);
    }

    @Bean
    public HeartbeatProperties heartbeatProperties() {
        return new HeartbeatProperties();
    }

    @Bean
    public ConsulDiscoveryClient consulDiscoveryClient() {
        return new ConsulDiscoveryClient();
    }
}
