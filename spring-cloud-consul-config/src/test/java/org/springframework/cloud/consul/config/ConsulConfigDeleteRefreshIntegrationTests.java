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

package org.springframework.cloud.consul.config;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for refreshing configuration when Consul keys are deleted.
 *
 * @author Yash Chauhan
 */
@DirtiesContext
public class ConsulConfigDeleteRefreshIntegrationTests {

	private static final String APP_NAME = "testConsulConfigDeleteRefresh";

	private static final String PREFIX = "_consulConfigDeleteRefreshTests__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static final String PROPERTY_NAME = "deleteRefreshProperty";

	private static final String KEY = ROOT + "/application/" + PROPERTY_NAME;

	private static final String PROPERTY_VALUE = "initialValue";

	private static ConfigurableApplicationContext context;

	private static ConfigurableEnvironment environment;

	private static ConsulClient testClient;

	@BeforeAll
	public static void setup() {
		ConsulTestcontainers.start();
		testClient = ConsulTestcontainers.client();

		testClient.deleteKVValues(PREFIX);
		testClient.setKVValue(KEY, PROPERTY_VALUE);

		context = new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE)
			.run("--spring.application.name=" + APP_NAME, "--spring.config.use-legacy-processing=true",
					"--spring.cloud.consul.host=" + ConsulTestcontainers.getHost(),
					"--spring.cloud.consul.port=" + ConsulTestcontainers.getPort(),
					"--spring.cloud.consul.config.prefixes=" + ROOT, "--spring.cloud.consul.config.watch.delay=1000",
					"--spring.cloud.consul.config.watch.refresh-on-delete=true");

		environment = context.getEnvironment();
	}

	@AfterAll
	public static void teardown() {
		if (context != null) {
			context.close();
		}
		if (testClient != null) {
			testClient.deleteKVValues(PREFIX);
		}
	}

	@Test
	public void configurationRefreshesWhenLastConsulKeyIsDeleted() throws Exception {
		assertThat(environment.getProperty(PROPERTY_NAME)).isEqualTo(PROPERTY_VALUE);

		CountDownLatch latch = context.getBean("deleteRefreshLatch", CountDownLatch.class);

		testClient.deleteKVValues(KEY);

		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);

		assertThat(receivedEvent).as("refresh event was not received after deleting the last Consul key").isTrue();

		assertThat(environment.getProperty(PROPERTY_NAME))
			.as("deleted configuration property should no longer be available")
			.isNull();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	static class Config {

		@Bean
		public CountDownLatch deleteRefreshLatch() {
			return new CountDownLatch(1);
		}

		@Bean
		public ApplicationListener<EnvironmentChangeEvent> deleteRefreshListener(CountDownLatch deleteRefreshLatch) {
			return event -> {
				if (event.getKeys().contains(PROPERTY_NAME)) {
					deleteRefreshLatch.countDown();
				}
			};
		}

	}

}
