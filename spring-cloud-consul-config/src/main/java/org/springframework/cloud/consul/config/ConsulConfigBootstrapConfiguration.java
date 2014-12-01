package org.springframework.cloud.consul.config;

import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Spencer Gibb
 */
@Configuration
@Import(ConsulAutoConfiguration.class)
public class ConsulConfigBootstrapConfiguration {
    @Bean
    public ConsulPropertySourceLocator consulPropertySourceLocator() {
        return new ConsulPropertySourceLocator();
    }
}
