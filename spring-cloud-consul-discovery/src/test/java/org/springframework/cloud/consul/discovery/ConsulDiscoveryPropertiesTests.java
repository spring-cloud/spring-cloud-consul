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

package org.springframework.cloud.consul.discovery;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ConsulDiscoveryProperties}.
 *
 * @author Chris Bono
 */
class ConsulDiscoveryPropertiesTests {

	private static final String DEFAULT_TAG = "defaultTag";

	private static final String MAP_TAG = "mapTag";

	private static final String MAP_DC = "mapDc";

	private static final String SERVICE_NAME_IN_MAP = "serviceNameInMap";

	private static final String SERVICE_NAME_NOT_IN_MAP = "serviceNameNotInMap";

	private final Map<String, String> serverListQueryTags = Collections.singletonMap(SERVICE_NAME_IN_MAP, MAP_TAG);

	private final Map<String, String> datacenters = Collections.singletonMap(SERVICE_NAME_IN_MAP, MAP_DC);

	private ConsulDiscoveryProperties properties;

	@BeforeEach
	void setUp() {
		this.properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		properties.setDefaultQueryTag(DEFAULT_TAG);
		properties.setServerListQueryTags(this.serverListQueryTags);
		properties.setDatacenters(this.datacenters);
	}

	@Test
	void getTagReturnsNullWhenNoDefaultAndNotInMap() {
		properties.setDefaultQueryTag(null);
		assertThat(properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP)).isNull();
	}

	@Test
	void getTagReturnsDefaultWhenNotInMap() {
		assertThat(properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP)).isEqualTo(DEFAULT_TAG);
	}

	@Test
	void getTagReturnsMapValueWhenInMap() {
		assertThat(properties.getQueryTagForService(SERVICE_NAME_IN_MAP)).isEqualTo(MAP_TAG);
	}

	@Test
	void getTagsReturnsNullWhenNoDefaultAndNotInMap() {
		properties.setDefaultQueryTag(null);
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_NOT_IN_MAP)).isNull();
	}

	@Test
	void getTagsReturnsNullWhenDefaultIsSetToEmptyStringAndNotInMap() {
		properties.setDefaultQueryTag("");
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_NOT_IN_MAP)).isNull();
	}

	@Test
	void getTagsReturnsDefaultWhenNotInMap() {
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_NOT_IN_MAP)).containsExactly(DEFAULT_TAG);
	}

	@Test
	void getTagsReturnsMapValueWhenInMap() {
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_IN_MAP)).containsExactly(MAP_TAG);
	}

	@Test
	void getTagsReturnsNullWhenMapValueIsSetToEmptyStringAndInMap() {
		properties.setServerListQueryTags(Collections.singletonMap(SERVICE_NAME_IN_MAP, ""));
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_IN_MAP)).isNull();
	}

	@Test
	void getTagsReturnsMultipleFromDefaultQueryTag() {
		properties.setDefaultQueryTag("foo,bar");
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_NOT_IN_MAP)).containsExactly("foo", "bar");
	}

	@Test
	void getTagsReturnsMultipleFromServerListMapEntry() {
		properties.setServerListQueryTags(Collections.singletonMap(SERVICE_NAME_IN_MAP, "foo,bar"));
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_IN_MAP)).containsExactly("foo", "bar");
	}

	@Test
	void getDcReturnsNullWhenNotInMap() {
		assertThat(properties.getDatacenters().get(SERVICE_NAME_NOT_IN_MAP)).isNull();
	}

	@Test
	void getTagsReturnsSingleTrimmedEntryFromTagWithExtraWhitespace() {
		properties.setServerListQueryTags(Collections.singletonMap(SERVICE_NAME_IN_MAP, "  foo  "));
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_IN_MAP)).containsExactly("foo");
	}

	@Test
	void getTagsReturnsMultipleTrimmedEntriesFromTagsWithExtraWhitespace() {
		properties.setServerListQueryTags(Collections.singletonMap(SERVICE_NAME_IN_MAP, "  foo  ,  bar   "));
		assertThat(properties.getQueryTagsForService(SERVICE_NAME_IN_MAP)).containsExactly("foo", "bar");
	}

	@Test
	void getDcReturnsMapValueWhenInMap() {
		assertThat(properties.getDatacenters().get(SERVICE_NAME_IN_MAP)).isEqualTo(MAP_DC);
	}

	@Test
	void addManagementTag() {
		properties.getManagementTags().add("newTag");
		assertThat(properties.getManagementTags()).containsOnly(ConsulDiscoveryProperties.MANAGEMENT, "newTag");
	}

}
