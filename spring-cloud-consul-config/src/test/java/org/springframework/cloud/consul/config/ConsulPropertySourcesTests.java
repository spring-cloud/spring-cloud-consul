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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ConsulPropertySources}.
 */
public class ConsulPropertySourcesTests {

	/**
	 * When using {@code spring.config.import=consul:/test}, the path has a leading slash
	 * which must be normalized before storing in the watch index map, otherwise
	 * ConfigWatch sends requests to Consul with a double-slash path (e.g.
	 * {@code //test/}) and receives a 301 redirect instead of the expected JSON.
	 */
	@Test
	public void createPropertySourceStoresNormalizedContextInIndex() {
		ConsulClient consul = mock(ConsulClient.class);
		Response<List<GetValue>> response = new Response<>(Collections.emptyList(), 1L, false, 1L);
		when(consul.getKVValues(eq("test/"), nullable(String.class), any(QueryParams.class))).thenReturn(response);

		ConsulConfigProperties properties = new ConsulConfigProperties();
		ConsulPropertySources sources = new ConsulPropertySources(properties,
				LogFactory.getLog(ConsulPropertySourcesTests.class));

		LinkedHashMap<String, Long> indexes = new LinkedHashMap<>();
		sources.createPropertySource("/test/", consul, indexes::put);

		assertThat(indexes).containsKey("test/");
		assertThat(indexes).doesNotContainKey("/test/");
	}

	@Test
	public void createPropertySourceFilesFormatStoresNormalizedContextInIndex() {
		ConsulClient consul = mock(ConsulClient.class);
		Response<GetValue> response = new Response<>(null, 1L, false, 1L);
		when(consul.getKVValue(eq("test.yml"), nullable(String.class))).thenReturn(response);

		ConsulConfigProperties properties = new ConsulConfigProperties();
		properties.setFormat(ConsulConfigProperties.Format.FILES);
		ConsulPropertySources sources = new ConsulPropertySources(properties,
				LogFactory.getLog(ConsulPropertySourcesTests.class));

		LinkedHashMap<String, Long> indexes = new LinkedHashMap<>();
		sources.createPropertySource("/test.yml", consul, indexes::put);

		assertThat(indexes).containsKey("test.yml");
		assertThat(indexes).doesNotContainKey("/test.yml");
	}

}
