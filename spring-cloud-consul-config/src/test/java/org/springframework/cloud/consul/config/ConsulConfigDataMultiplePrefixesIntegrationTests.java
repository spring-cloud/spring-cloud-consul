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

import com.ecwid.consul.v1.ConsulClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
@DirtiesContext
public class ConsulConfigDataMultiplePrefixesIntegrationTests {

	private static final String APP_NAME = "testConsulConfigData";

	private static final String PREFIX = "_configDataMultiplePrefixesIntegrationTests_config__";

	private static final String PREFIX2 = "_configDataMultiplePrefixesIntegrationTests_config2__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static final String ROOT2 = PREFIX2 + UUID.randomUUID();

	private static final String VALUE1 = "testPropVal";

	private static final String TEST_PROP = "testProp";

	private static final String TEST_PROP_CANONICAL = "test-prop";

	private static final String KEY1 = ROOT + "/application/" + TEST_PROP;

	private static final String VALUE2 = "testPropVal2";

	private static final String TEST_PROP2 = "testProp2";

	private static final String TEST_PROP2_CANONICAL = "test-prop2";

	private static final String KEY2 = ROOT2 + "/application/" + TEST_PROP2;

	private static ConfigurableApplicationContext context;

	private static ConfigurableEnvironment environment;

	private static ConsulClient client;

	@BeforeAll
	public static void setup() {
		ConsulTestcontainers.start();
		client = ConsulTestcontainers.client();
		client.deleteKVValues(PREFIX);
		client.deleteKVValues(PREFIX2);
		client.setKVValue(KEY1, VALUE1);
		client.setKVValue(KEY2, VALUE2);

		context = new SpringApplicationBuilder(Config.class).web(WebApplicationType.NONE)
			.run("--logging.level.org.springframework.cloud.consul.config.ConfigWatch=TRACE",
					"--spring.application.name=" + APP_NAME,
					"--spring.config.import=consul:" + ConsulTestcontainers.getHost() + ":"
							+ ConsulTestcontainers.getPort(),
					"--spring.cloud.consul.config.prefixes=" + ROOT + "," + ROOT2,
					"--spring.cloud.consul.config.watch.delay=10", "--spring.cloud.consul.config.watch.wait-time=1");

		client = context.getBean(ConsulClient.class);
		environment = context.getEnvironment();
	}

	@AfterAll
	public static void teardown() {
		client.deleteKVValues(PREFIX);
		client.deleteKVValues(PREFIX2);
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void propertyLoaded() {
		String testProp = environment.getProperty(TEST_PROP_CANONICAL);
		assertThat(testProp).as(TEST_PROP + " was wrong").isEqualTo(VALUE1);
		String testProp2 = environment.getProperty(TEST_PROP2_CANONICAL);
		assertThat(testProp2).as(TEST_PROP2 + " was wrong").isEqualTo(VALUE2);
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

	}

}
