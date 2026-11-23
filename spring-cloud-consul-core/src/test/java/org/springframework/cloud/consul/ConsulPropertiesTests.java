/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.consul;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the validation of {@link ConsulProperties}.
 */
class ConsulPropertiesTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(PropertiesConfiguration.class);

	@Test
	void blockingQueryReadTimeoutDefaultsToEleven() {
		this.contextRunner
			.run(context -> assertThat(context.getBean(ConsulProperties.class).getBlockingQueryReadTimeout())
				.isEqualTo(ConsulProperties.DEFAULT_BLOCKING_QUERY_READ_TIMEOUT));
	}

	@Test
	void blockingQueryReadTimeoutCanBeConfigured() {
		this.contextRunner.withPropertyValues("spring.cloud.consul.blocking-query-read-timeout=30m")
			.run(context -> assertThat(context.getBean(ConsulProperties.class).getBlockingQueryReadTimeout())
				.isEqualTo(Duration.ofMinutes(30)));
	}

	@Test
	void zeroBlockingQueryReadTimeoutIsRejected() {
		this.contextRunner.withPropertyValues("spring.cloud.consul.blocking-query-read-timeout=0s")
			.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void negativeBlockingQueryReadTimeoutIsRejected() {
		this.contextRunner.withPropertyValues("spring.cloud.consul.blocking-query-read-timeout=-1s")
			.run(context -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(ConsulProperties.class)
	static class PropertiesConfiguration {

	}

}
