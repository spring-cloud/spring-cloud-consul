package org.springframework.cloud.consul.client;

import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Spencer Gibb
 */
@Configuration
@Import(ConsulAutoConfiguration.class)
public class TestClientConfiguration {
}
