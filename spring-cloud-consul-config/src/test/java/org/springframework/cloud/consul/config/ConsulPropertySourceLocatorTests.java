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

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ecwid.consul.v1.ConsulClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
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
 * @author Spencer Gibb
 */
@DirtiesContext
public class ConsulPropertySourceLocatorTests {

	private static final String APP_NAME = "testConsulPropertySourceLocator";

	private static final String PREFIX = "_propertySourceLocatorTests_config__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static final String VALUE1 = "testPropVal";

	private static final String TEST_PROP = "testProp";

	private static final String TEST_PROP_CANONICAL = "test-prop";

	private static final String KEY1 = ROOT + "/application/" + TEST_PROP;

	private static final String VALUE2 = "testPropVal2";

	private static final String TEST_PROP2 = "testProp2";

	private static final String TEST_PROP2_CANONICAL = "test-prop2";

	private static final String KEY2 = ROOT + "/application/" + TEST_PROP2;

	private static final String TEST_PROP3 = "testProp3";

	private static final String TEST_PROP3_CANONICAL = "test-prop3";

	private static final String KEY3 = ROOT + "/" + APP_NAME + "/" + TEST_PROP3;

	private static ConfigurableApplicationContext context;

	private static ConfigurableEnvironment environment;

	private static ConsulClient client;

	@BeforeAll
	public static void setup() {
		ConsulTestcontainers.start();
		client = ConsulTestcontainers.client();
		client.deleteKVValues(PREFIX);
		client.setKVValue(KEY1, VALUE1);
		client.setKVValue(KEY2, VALUE2);

		context = new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE).run(
				"--spring.application.name=" + APP_NAME, "--spring.config.use-legacy-processing=true",
				"--spring.cloud.consul.host=" + ConsulTestcontainers.getHost(),
				"--spring.cloud.consul.port=" + ConsulTestcontainers.getPort(),
				"--spring.cloud.consul.config.prefix=" + ROOT, "--spring.cloud.consul.config.watch.delay=10");

		client = context.getBean(ConsulClient.class);
		environment = context.getEnvironment();
	}

	@AfterAll
	public static void teardown() {
		client.deleteKVValues(PREFIX);
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void propertyLoaded() {
		String testProp = environment.getProperty(TEST_PROP2_CANONICAL);
		assertThat(testProp).as("testProp was wrong").isEqualTo(VALUE2);
	}

	@Test
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = environment.getProperty(TEST_PROP_CANONICAL);
		assertThat(testProp).as("testProp was wrong").isEqualTo(VALUE1);

		client.setKVValue(KEY1, "testPropValUpdate");

		CountDownLatch latch = context.getBean("countDownLatch1", CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat(receivedEvent).as("listener didn't receive event").isTrue();

		testProp = environment.getProperty(TEST_PROP_CANONICAL);
		assertThat(testProp).as("testProp was wrong after update").isEqualTo("testPropValUpdate");
	}

	@Test
	public void contextDoesNotExistThenExists() throws Exception {
		String testProp = environment.getProperty(TEST_PROP3_CANONICAL);
		assertThat(testProp).as("testProp was wrong").isNull();

		client.setKVValue(KEY3, "testPropValInsert");

		CountDownLatch latch = context.getBean("countDownLatch2", CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat(receivedEvent).as("listener didn't receive event").isTrue();

		testProp = environment.getProperty(TEST_PROP3_CANONICAL);
		assertThat(testProp).as(TEST_PROP3 + " was wrong after update").isEqualTo("testPropValInsert");
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config implements ApplicationListener<EnvironmentChangeEvent> {

		@Bean
		public CountDownLatch countDownLatch1() {
			return new CountDownLatch(1);
		}

		@Bean
		public CountDownLatch countDownLatch2() {
			return new CountDownLatch(1);
		}

		@Override
		public void onApplicationEvent(EnvironmentChangeEvent event) {
			if (event.getKeys().contains(TEST_PROP)) {
				countDownLatch1().countDown();
			}
			else if (event.getKeys().contains(TEST_PROP3)) {
				countDownLatch2().countDown();
			}
		}

	}

}
