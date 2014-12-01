package org.springframework.cloud.consul.discovery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
public class ConsulDiscoveryAutoConfiguration {
    @Bean
    public ConsulLifecycle consulLifecycle() {
        return new ConsulLifecycle();
    }

    @Bean
    public ConsulLoadBalancerClient consulLoadBalancerClient() {
        return new ConsulLoadBalancerClient();
    }

    @Bean
    public ConsulDiscoveryClient consulDiscoveryClient() {
        return new ConsulDiscoveryClient();
    }
}
