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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import org.yaml.snakeyaml.error.YAMLException;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.kv.GetValue;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for how {@link ConsulPropertySource} parses YAML documents.
 *
 * @author Asish Kumar
 */
class ConsulPropertySourceYamlTests {

	/** One more alias than the SnakeYAML default limit of 50. */
	private static final int ALIAS_COUNT = 51;

	/** One more code point than the SnakeYAML default limit of 3 MB. */
	private static final int CODE_POINT_COUNT = 3 * 1024 * 1024 + 1;

	@Test
	void yamlFormatLoadsDocumentExceedingDefaultAliasLimit() {
		GetValue data = value("config/app/data", yamlWithAliases());
		ConsulClient consul = mock(ConsulClient.class);
		when(consul.getKVValues(eq("config/app/"), isNull())).thenReturn(ResponseEntity.ok(List.of(data)));
		ConsulConfigProperties properties = new ConsulConfigProperties();
		properties.setFormat(ConsulConfigProperties.Format.YAML);

		ConsulPropertySource source = new ConsulPropertySource("config/app", consul, properties);
		source.init();

		assertThat(source.getProperty("service" + (ALIAS_COUNT - 1) + ".timeout")).isEqualTo(5);
	}

	@Test
	void filesFormatLoadsDocumentExceedingDefaultAliasLimit() {
		ConsulFilesPropertySource source = new ConsulFilesPropertySource("config/app.yml", mock(ConsulClient.class),
				new ConsulConfigProperties());

		source.init(value("config/app.yml", yamlWithAliases()));

		assertThat(source.getProperty("service" + (ALIAS_COUNT - 1) + ".timeout")).isEqualTo(5);
	}

	@Test
	void yamlLoadsDocumentExceedingDefaultCodePointLimit() {
		String value = "x".repeat(CODE_POINT_COUNT);

		assertThat(generateProperties("foo: " + value)).containsEntry("foo", value);
	}

	@Test
	void yamlWithDuplicateKeysIsRejected() {
		assertThatExceptionOfType(DuplicateKeyException.class)
			.isThrownBy(() -> generateProperties("foo: 1\nfoo: 2"));
	}

	@Test
	void yamlWithGlobalTagIsRejected() {
		assertThatExceptionOfType(YAMLException.class)
			.isThrownBy(() -> generateProperties("foo: !!java.util.Date {}"))
			.withMessageContaining("Global tag is not allowed");
	}

	private Properties generateProperties(String yaml) {
		return new ConsulPropertySource("config/app", mock(ConsulClient.class), new ConsulConfigProperties())
			.generateProperties(yaml, ConsulConfigProperties.Format.YAML);
	}

	private static String yamlWithAliases() {
		StringBuilder yaml = new StringBuilder("defaults: &defaults\n  timeout: 5\n");
		for (int i = 0; i < ALIAS_COUNT; i++) {
			yaml.append("service").append(i).append(": *defaults\n");
		}
		return yaml.toString();
	}

	private static GetValue value(String key, String content) {
		GetValue value = new GetValue();
		value.setKey(key);
		value.setValue(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
		return value;
	}

}
