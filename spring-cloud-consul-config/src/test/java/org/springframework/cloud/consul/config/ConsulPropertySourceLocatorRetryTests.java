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

import com.ecwid.consul.transport.TransportException;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceLocatorRetryTests {

	@Rule
	public OutputCaptureRule output = new OutputCaptureRule();

	@Test
	public void testRetry() {
		assertThatThrownBy(() -> {
			new SpringApplicationBuilder(Config.class).properties(
					"spring.application.name=testConsulPropertySourceLocatorRetry",
					"spring.config.use-legacy-processing=true",
					"spring.cloud.consul.host=53210a7c-4809-42cb-8b30-057d2db85fcc",
					"logging.level.org.springframework.retry=TRACE", "server.port=0", "spring.cloud.consul.port=65530",
					"spring.cloud.consul.retry.maxAttempts=1", "spring.cloud.consul.config.failFast=true").run();
			fail("Did not throw expected exception");
		}).hasCauseInstanceOf(TransportException.class);
		assertThat(output).contains("RetryContext retrieved");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	static class Config {

	}

}
