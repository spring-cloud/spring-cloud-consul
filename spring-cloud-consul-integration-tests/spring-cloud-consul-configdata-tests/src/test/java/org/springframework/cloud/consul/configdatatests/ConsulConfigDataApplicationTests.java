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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
public class ConsulConfigDataApplicationTests {

	private static final String APP_NAME = "testConsulConfigDataIntegration";

	private static final String PREFIX = "_configDataIntegrationTests_config__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static ConfigurableApplicationContext context;

	@BeforeAll
	public static void setup() {
		ConsulTestcontainers.start();

		SpringApplication application = new SpringApplication(ConsulConfigDataApplication.class);
		context = application.run("--spring.application.name=" + APP_NAME,
				"--spring.config.import=optional:consul:" + ConsulTestcontainers.getHost() + ":"
						+ ConsulTestcontainers.getPort(),
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
	}

}
