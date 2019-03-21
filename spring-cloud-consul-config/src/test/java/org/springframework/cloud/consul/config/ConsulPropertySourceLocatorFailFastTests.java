/*
 * Copyright 2013-2016 the original author or authors.
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
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ConsulPropertySourceLocatorFailFastTests.Config.class)
@WebIntegrationTest(value = {"spring.application.name=testConsulPropertySourceLocatorFailFast",
		"spring.cloud.consul.host=53210a7c-4809-42cb-8b30-057d2db85fcc",
		"spring.cloud.consul.port=65530",
		"spring.cloud.consul.retry.maxAttempts=0",
		"spring.cloud.consul.config.failFast=false"}, randomPort = true)
public class ConsulPropertySourceLocatorFailFastTests {

	@Configuration
	@EnableAutoConfiguration
	static class Config {
	}

	@Test
	public void testFailFastFalse() {
	}

}
