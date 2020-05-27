package org.springframework.cloud.consul.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *Tests for {@link RibbonConsulAutoConfiguration}.
 *
 * @author Flora Kalisa
 */

public class RibbonConsulAutoConfigurationTests {

	ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(
			RibbonAutoConfiguration.class,
			RibbonConsulAutoConfiguration.class));
	@Test
	public void shouldWorkWithDefaults() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(LoadBalancerClient.class);
		});
	}

	@Test
	public void shouldNotHaveConsulRibbonClientWhenConsulRibbonDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.ribbon.enabled=false")
			.run(context -> {
				assertThat(context).doesNotHaveBean(ConsulRibbonClientConfiguration.class);
			});
	}

	@Test
	public void shouldNotHaveConsulRibbonClientWhenConsulDiscoveryDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.discovery.enabled=false")
			.run(context -> {
				assertThat(context).doesNotHaveBean(ConsulRibbonClientConfiguration.class);
			});
	}

	@Test
	public void shouldHaveConsulRibbonClientWhenConsulDiscoveryAndConsulRibbonEnabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.discovery.enabled=true", "spring.cloud.consul.ribbon.enabled=true")
			.run(context -> {
				assertThat(context).doesNotHaveBean(ConsulRibbonClientConfiguration.class);
			});
	}
}
