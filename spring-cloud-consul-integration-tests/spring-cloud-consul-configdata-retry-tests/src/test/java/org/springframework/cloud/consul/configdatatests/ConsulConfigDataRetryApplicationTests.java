/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.consul.configdatatests;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.consul.config.ConsulBootstrapper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
public class ConsulConfigDataRetryApplicationTests {

	private static final String APP_NAME = "testConsulConfigDataRetryIntegration";

	private static final String PREFIX = "_configDataRetryIntegrationTests_config__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static ConfigurableApplicationContext context;

	private static final AtomicInteger count = new AtomicInteger();

	@BeforeAll
	public static void setup() {
		context = new SpringApplicationBuilder(ConsulConfigDataRetryApplication.class)
			.addBootstrapRegistryInitializer(registry -> {
				registry.register(ConsulBootstrapper.LoaderInterceptor.class, context -> {
					RetryTemplate retryTemplate = context.get(RetryTemplate.class);
					if (retryTemplate != null) {
						return loadContext -> retryTemplate.execute(retryContext -> {
							count.incrementAndGet();
							return loadContext.getInvocation()
								.apply(loadContext.getLoaderContext(), loadContext.getResource());
						});
					}
					// disabled
					return null;
				});
			})
			.run("--spring.application.name=" + APP_NAME, "--spring.cloud.consul.retry.enabled=true",
					"--spring.cloud.consul.retry.max-attempts=2",
					// non-existent consul host and port
					"--spring.config.import=optional:consul:somehost:1234",
					"--spring.cloud.consul.config.prefix=" + ROOT, "--spring.cloud.consul.config.watch.delay=10");

	}

	@AfterAll
	public static void teardown() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void contextLoads() {
		// four default contexts times two retries
		assertThat(count.get()).as("Retry failed").isGreaterThanOrEqualTo(8);
	}

}
