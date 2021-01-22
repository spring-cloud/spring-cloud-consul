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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.consul.config.ConsulConfigProperties.Format;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tommy Karlsson
 */
public class ConsulConfigPropertiesTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(Config.class);

	@Test
	public void testDefaultContextFormatFallback() {
		contextRunner.withPropertyValues("spring.cloud.consul.config.format=" + Format.PROPERTIES.name())
				.run(context -> {
					ConsulConfigProperties config = context.getBean(ConsulConfigProperties.class);
					assertThat(config.getFormat()).as("Format did not match").isEqualTo(Format.PROPERTIES);
					assertThat(config.getDefaultContextFormat()).as("Default context format did not match")
							.isEqualTo(Format.PROPERTIES);
				});
	}

	@Test
	public void testDefaultContextFormatConfigured() {
		contextRunner.withUserConfiguration(ConsulConfigBootstrapConfiguration.class)
				.withPropertyValues("spring.cloud.consul.config.format=" + Format.PROPERTIES.name(),
						"spring.cloud.consul.config.default-context-format=" + Format.YAML.name())
				.run(context -> {
					ConsulConfigProperties config = context.getBean(ConsulConfigProperties.class);
					assertThat(config.getFormat()).as("Format did not match").isEqualTo(Format.PROPERTIES);
					assertThat(config.getDefaultContextFormat()).as("Default context format did not match")
							.isEqualTo(Format.YAML);
				});
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(ConsulConfigProperties.class)
	static class Config {

	}

}
