package org.springframework.cloud.consul.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

/**
 * @author Edvin Eriksson
 */
public class ConsulConfigBootstrapConfigurationTests {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	/**
	 * Tests that the auto-config bean backs off if a user provided their own
	 */
	@Test
	public void testConfigPropsBeanBacksOff() {
		contextRunner
			.withUserConfiguration(TestConfig.class)
			.withUserConfiguration(ConsulConfigBootstrapConfiguration.class)
			.run(context -> {
				ConsulConfigProperties config = context.getBean(ConsulConfigProperties.class);
				Assert.assertEquals("Prefix did not match", "platform-config", config.getPrefix());
				Assert.assertEquals("Default context did not match", "defaults", config.getDefaultContext());
			});
	}

	/**
	 * Tests that the auto-config bean kicks in if the user did not provide any custom bean.
	 */
	@Test
	public void testConfigPropsBeanKicksIn() {
		contextRunner
			.withUserConfiguration(ConsulConfigBootstrapConfiguration.class)
			.run(context -> {
				ConsulConfigProperties config = context.getBean(ConsulConfigProperties.class);
				Assert.assertEquals("Prefix did not match", "config", config.getPrefix());
				Assert.assertEquals("Default context did not match", "application", config.getDefaultContext());
			});
	}
}

/**
 * Test config that simulates a "user provided bean"
 */
class TestConfig {
	@Bean
	public ConsulConfigProperties consulConfigProperties() {
		ConsulConfigProperties config = new ConsulConfigProperties();
		config.setPrefix("platform-config");
		config.setDefaultContext("defaults");
		return config;
	}
}