package org.springframework.cloud.consul.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author Spencer Gibb
 */
@Configuration
@Import(ConsulAutoConfiguration.class)
public class ConsulConfigBootstrapConfiguration {

	@Autowired
	private ConfigurableEnvironment environment;

	@Bean
	public ConfigClientProperties configClientProperties() {
		ConfigClientProperties client = new ConfigClientProperties(environment);
		return client;
	}

    @Bean
    public ConsulPropertySourceLocator consulPropertySourceLocator() {
        return new ConsulPropertySourceLocator();
    }
}
