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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
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

	private ConfigurableApplicationContext context;

	private ConfigurableEnvironment environment;

	private ConsulClient client;

	@Before
	public void setup() {
		ConsulTestcontainers.start();
		this.client = ConsulTestcontainers.client();
		this.client.deleteKVValues(PREFIX);
		this.client.setKVValue(KEY1, VALUE1);
		this.client.setKVValue(KEY2, VALUE2);

		this.context = new SpringApplicationBuilder(Config.class)
				.web(WebApplicationType.NONE).run("--SPRING_APPLICATION_NAME=" + APP_NAME,
						"--spring.cloud.consul.host=" + ConsulTestcontainers.getHost(),
						"--spring.cloud.consul.port=" + ConsulTestcontainers.getPort(),
						"--spring.cloud.consul.config.prefix=" + ROOT,
						"spring.cloud.consul.config.watch.delay=10");

		this.client = this.context.getBean(ConsulClient.class);
		this.environment = this.context.getEnvironment();
	}

	@After
	public void teardown() {
		this.client.deleteKVValues(PREFIX);
		this.context.close();
	}

	@Test
	public void propertyLoaded() throws Exception {
		String testProp = this.environment.getProperty(TEST_PROP2_CANONICAL);
		assertThat(testProp).as("testProp was wrong").isEqualTo(VALUE2);
	}

	@Test
	@Ignore // FIXME broken tests with boot 2.0.0
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = this.environment.getProperty(TEST_PROP_CANONICAL);
		assertThat(testProp).as("testProp was wrong").isEqualTo(VALUE1);

		this.client.setKVValue(KEY1, "testPropValUpdate");

		CountDownLatch latch = this.context.getBean("countDownLatch1",
				CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat(receivedEvent).as("listener didn't receive event").isTrue();

		testProp = this.environment.getProperty(TEST_PROP_CANONICAL);
		assertThat(testProp).as("testProp was wrong after update")
				.isEqualTo("testPropValUpdate");
	}

	@Test
	@Ignore // FIXME broken tests with boot 2.0.0
	public void contextDoesNotExistThenExists() throws Exception {
		String testProp = this.environment.getProperty(TEST_PROP3_CANONICAL);
		assertThat(testProp).as("testProp was wrong").isNull();

		this.client.setKVValue(KEY3, "testPropValInsert");

		CountDownLatch latch = this.context.getBean("countDownLatch2",
				CountDownLatch.class);
		boolean receivedEvent = latch.await(15, TimeUnit.SECONDS);
		assertThat(receivedEvent).as("listener didn't receive event").isTrue();

		testProp = this.environment.getProperty(TEST_PROP3_CANONICAL);
		assertThat(testProp).as(TEST_PROP3 + " was wrong after update")
				.isEqualTo("testPropValInsert");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	static class Config {

		@Bean
		public CountDownLatch countDownLatch1() {
			return new CountDownLatch(1);
		}

		@Bean
		public CountDownLatch countDownLatch2() {
			return new CountDownLatch(1);
		}

		@EventListener
		public void handle(EnvironmentChangeEvent event) {
			if (event.getKeys().contains(TEST_PROP)) {
				countDownLatch1().countDown();
			}
			else if (event.getKeys().contains(TEST_PROP3)) {
				countDownLatch2().countDown();
			}
		}

	}

}
