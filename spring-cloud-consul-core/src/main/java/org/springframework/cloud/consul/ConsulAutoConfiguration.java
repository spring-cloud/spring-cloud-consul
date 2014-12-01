package org.springframework.cloud.consul;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
public class ConsulAutoConfiguration {
    protected Feign.Builder builder = Feign.builder()
            .logger(new Logger.JavaLogger())
            .errorDecoder(new ConsulErrorDecoder())
            .decoder(new JacksonDecoder())
            .encoder(new JacksonEncoder());

    @Bean
    @ConditionalOnMissingBean
    public ConsulProperties consulProperties() {
        return new ConsulProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentClient agentClient() {
        return builder.target(AgentClient.class, consulProperties().getUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    public CatalogClient catalogClient() {
        return builder.target(CatalogClient.class, consulProperties().getUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    public KeyValueClient kvClient() {
        return builder.target(KeyValueClient.class, consulProperties().getUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventClient eventClient() {
        return builder.target(EventClient.class, consulProperties().getUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventService eventService() {
        return new EventService();
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
