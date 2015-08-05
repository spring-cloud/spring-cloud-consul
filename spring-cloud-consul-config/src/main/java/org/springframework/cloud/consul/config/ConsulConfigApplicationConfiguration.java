package org.springframework.cloud.consul.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.config.RefreshEndpoint;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import(ConsulAutoConfiguration.class)
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty(name = "spring.cloud.consul.config.watch", matchIfMissing = false)
public class ConsulConfigApplicationConfiguration {

	@Autowired
	ConsulConfigProperties consulConfigProperties;
	
	@Autowired
    private RefreshEndpoint refreshEndpoint;

	@Bean
	public ConsulConfigWatch consulConfigWatch() {
		return new ConsulConfigWatch(consulConfigProperties);
	}
	
    @Bean
    public ConsulConfigurationListener consulConfigurationListener() {
        return new ConsulConfigurationListener(refreshEndpoint);
    }
}
