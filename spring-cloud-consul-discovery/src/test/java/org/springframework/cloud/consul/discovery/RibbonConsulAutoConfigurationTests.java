package org.springframework.cloud.consul.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
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
			assertThat(context).hasSingleBean(RibbonConsulAutoConfiguration.class);
		});
	}

	@Test
	public void shouldNotHaveRibbonConsulAutoConfigWhenConsulRibbonDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.ribbon.enabled=false")
			.run(context -> {
				assertThat(context).doesNotHaveBean(RibbonConsulAutoConfiguration.class);
			});
	}

	@Test
	public void shouldNotHaveRibbonConsulAutoConfigWhenConsulDiscoveryDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.discovery.enabled=false")
			.run(context -> {
				assertThat(context).doesNotHaveBean(RibbonConsulAutoConfiguration.class);
			});
	}
}
