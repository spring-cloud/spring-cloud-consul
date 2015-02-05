package org.springframework.cloud.consul;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(name = "spring.consul.enabled", matchIfMissing = true)
public class ConsulAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsulProperties consulProperties() {
        return new ConsulProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsulClient consulClient() {
        return new ConsulClient(consulProperties().getHost(), consulProperties().getPort());
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsulEndpoint consulEndpoint() {
        return new ConsulEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsulHealthIndicator consulHealthIndicator() {
        return new ConsulHealthIndicator();
    }
}
