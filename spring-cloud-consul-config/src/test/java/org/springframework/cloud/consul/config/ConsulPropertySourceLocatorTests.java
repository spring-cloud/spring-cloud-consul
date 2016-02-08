/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.concurrent.atomic.AtomicInteger;

import com.ecwid.consul.v1.ConsulClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceLocatorTests {
	public static final String PREFIX = "_propertySourceLocatorTests_config__";
	public static final String ROOT = PREFIX + UUID.randomUUID();
	private ConfigurableApplicationContext context;
	public static final String KEY = ROOT + "/application/testProp";

	@Configuration
	@EnableAutoConfiguration
	static class Config {
		@Bean
		public RefreshEndpoint refreshEndpoint(ConfigurableApplicationContext context,
											   RefreshScope scope) {
			RefreshEndpoint endpoint = new TestRefreshEndpoint(context, scope);
			return endpoint;
		}
	}

	static class TestRefreshEndpoint extends RefreshEndpoint {
		private CountDownLatch successLatch = new CountDownLatch(1);
		private CountDownLatch toManyLatch = new CountDownLatch(1);
		private AtomicInteger count = new AtomicInteger();

		public TestRefreshEndpoint( ConfigurableApplicationContext context, RefreshScope scope) {
			super(context, scope);
		}

		@Override
		public synchronized String[] refresh() {
			String[] keys = super.refresh();
			if (this.count.incrementAndGet() == 1) {
				this.successLatch.countDown();
			} else {
				this.toManyLatch.countDown();
			}
			return keys;
		}
	}

	private ConfigurableEnvironment environment;
	private ConsulClient client;
	private ConsulProperties properties;

	@Before
	public void setup() {
		this.properties = new ConsulProperties();
		this.client = new ConsulClient(properties.getHost(), properties.getPort());
		this.client.deleteKVValues(PREFIX);
		this.client.setKVValue(KEY, "testPropVal");


		this.context = new SpringApplicationBuilder(Config.class)
				.web(false)
				.run("--spring.spring.application.name=testConsulPropertySourceLocator",
						"--spring.cloud.consul.config.prefix="+ROOT,
						"spring.cloud.consul.config.watch.delay=1");

		this.client = context.getBean(ConsulClient.class);
		this.properties = context.getBean(ConsulProperties.class);
		this.environment = context.getEnvironment();
	}

	@After
	public void teardown() {
		this.client.deleteKVValues(PREFIX);
	}

	@Test
	public void propertyLoadedAndUpdated() throws Exception {
		String testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong", testProp, is(equalTo("testPropVal")));

		this.client.setKVValue(KEY, "testPropValUpdate");

		TestRefreshEndpoint endpoint = this.context.getBean(TestRefreshEndpoint.class);
		boolean receivedEvent = endpoint.successLatch.await(15, TimeUnit.SECONDS);
		assertThat("listener didn't receive event", receivedEvent, is(true));

		testProp = this.environment.getProperty("testProp");
		assertThat("testProp was wrong after update", testProp, is(equalTo("testPropValUpdate")));

		boolean receivedExtraEvent = endpoint.toManyLatch.await(2, TimeUnit.SECONDS);
		assertThat("refresh called to many times", receivedExtraEvent, is(false));
	}
}
