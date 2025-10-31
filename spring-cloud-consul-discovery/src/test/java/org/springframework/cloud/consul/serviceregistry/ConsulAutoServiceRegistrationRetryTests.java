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

package org.springframework.cloud.consul.serviceregistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Spencer Gibb
 * @author Venil Noronha
 */
@DirtiesContext
@ContextConfiguration(initializers = ConsulTestcontainers.class)
@ExtendWith(OutputCaptureExtension.class)
public class ConsulAutoServiceRegistrationRetryTests {

	// @Disabled
	@Test
	public void testRetry(CapturedOutput output) {
		assertThatThrownBy(() -> {
			try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfig.class)
				.properties("spring.application.name=testregistrationretry",
						"spring.jmx.default-domain=testautoregretry", "spring.cloud.consul.retry.max-attempts=2",
						"logging.level.org.springframework.retry=DEBUG", "server.port=0")
				.run()) {
				// try with resources
			}
		}).cause().hasMessageContaining("Connection refused");
		assertThat(output).contains("Retry: count=");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	protected static class TestConfig {

	}

}
