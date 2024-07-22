/*
 * Copyright 2015-2021 the original author or authors.
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

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author Ryan Baxter
 */
class ConsulConfigDataMissingEnvironmentPostProcessorTests {

	@Test
	void noSpringConfigImport() {
		MockEnvironment environment = new MockEnvironment();
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatThrownBy(() -> processor.postProcessEnvironment(environment, app))
			.isInstanceOf(ConsulConfigDataMissingEnvironmentPostProcessor.ImportException.class);
	}

	@Test
	void boostrap() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.cloud.bootstrap.enabled", "true");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

	@Test
	void legacy() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.use-legacy-processing", "true");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

	@Test
	void configNotEnabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.cloud.consul.enabled", "false");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

	@Test
	void importCheckNotEnabled() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.cloud.consul.config.import-check.enabled", "false");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

	@Test
	void importSinglePropertySource() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.import", "consul:http://localhost:8888");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

	@Test
	void importMultiplePropertySource() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.import", "consul:http://localhost:8888,file:./app.properties");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

	@Test
	void importMultiplePropertySourceAsList() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("spring.config.import[0]", "consul:http://localhost:8888");
		environment.setProperty("spring.config.import[1]", "file:./app.properties");
		SpringApplication app = mock(SpringApplication.class);
		ConsulConfigDataMissingEnvironmentPostProcessor processor = new ConsulConfigDataMissingEnvironmentPostProcessor();
		assertThatCode(() -> processor.postProcessEnvironment(environment, app)).doesNotThrowAnyException();
	}

}
