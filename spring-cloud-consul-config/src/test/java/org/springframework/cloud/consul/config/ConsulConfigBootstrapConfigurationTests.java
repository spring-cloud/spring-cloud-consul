/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.config;

import org.junit.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Edvin Eriksson
 */
public class ConsulConfigBootstrapConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	/**
	 * Tests that the auto-config bean backs off if a user provided their own.
	 */
	@Test
	public void testConfigPropsBeanBacksOff() {
		this.contextRunner.withUserConfiguration(TestConfig.class).withInitializer(new ConsulTestcontainers())
				.withUserConfiguration(ConsulConfigBootstrapConfiguration.class).run(context -> {
					ConsulConfigProperties config = context.getBean(ConsulConfigProperties.class);
					assertThat(config.getPrefix()).as("Prefix did not match").isEqualTo("platform-config");
					assertThat(config.getDefaultContext()).as("Default context did not match").isEqualTo("defaults");
				});
	}

	/**
	 * Tests that the auto-config bean kicks in if the user did not provide any custom
	 * bean.
	 */
	@Test
	public void testConfigPropsBeanKicksIn() {
		this.contextRunner.withUserConfiguration(ConsulConfigBootstrapConfiguration.class)
				.withInitializer(new ConsulTestcontainers()).run(context -> {
					ConsulConfigProperties config = context.getBean(ConsulConfigProperties.class);
					assertThat(config.getPrefix()).as("Prefix did not match").isEqualTo("config");
					assertThat(config.getDefaultContext()).as("Default context did not match").isEqualTo("application");
				});
	}

	/**
	 * Test config that simulates a "user provided bean".
	 */
	private static class TestConfig {

		@Bean
		public ConsulConfigProperties consulConfigProperties() {
			ConsulConfigProperties config = new ConsulConfigProperties();
			config.setPrefix("platform-config");
			config.setDefaultContext("defaults");
			return config;
		}

	}

}
