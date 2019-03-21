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

package org.springframework.cloud.consul.discovery;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsulDiscoveryPropertiesTests {

	private static final String DEFAULT_TAG = "defaultTag";

	private static final String MAP_TAG = "mapTag";

	private static final String MAP_DC = "mapDc";

	private static final String SERVICE_NAME_IN_MAP = "serviceNameInMap";

	private static final String SERVICE_NAME_NOT_IN_MAP = "serviceNameNotInMap";

	private final Map<String, String> serverListQueryTags = Collections
			.singletonMap(SERVICE_NAME_IN_MAP, MAP_TAG);

	private final Map<String, String> datacenters = Collections
			.singletonMap(SERVICE_NAME_IN_MAP, MAP_DC);

	private ConsulDiscoveryProperties properties;

	@Before
	public void setUp() {
		this.properties = new ConsulDiscoveryProperties(
				new InetUtils(new InetUtilsProperties()));
		this.properties.setDefaultQueryTag(DEFAULT_TAG);
		this.properties.setServerListQueryTags(this.serverListQueryTags);
		this.properties.setDatacenters(this.datacenters);
	}

	@Test
	public void testReturnsNullWhenNoDefaultAndNotInMap() {
		this.properties.setDefaultQueryTag(null);

		assertThat(this.properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP))
				.isNull();
	}

	@Test
	public void testGetTagReturnsDefaultWhenNotInMap() {
		assertThat(this.properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP))
				.isEqualTo(DEFAULT_TAG);
	}

	@Test
	public void testGetTagReturnsMapValueWhenInMap() {
		assertThat(this.properties.getQueryTagForService(SERVICE_NAME_IN_MAP))
				.isEqualTo(MAP_TAG);
	}

	@Test
	public void testGetDcReturnsNullWhenNotInMap() {
		assertThat(this.properties.getDatacenters().get(SERVICE_NAME_NOT_IN_MAP))
				.isNull();
	}

	@Test
	public void testGetDcReturnsMapValueWhenInMap() {
		assertThat(this.properties.getDatacenters().get(SERVICE_NAME_IN_MAP))
				.isEqualTo(MAP_DC);
	}

	@Test
	public void testAddManagementTag() {
		this.properties.getManagementTags().add("newTag");
		assertThat(this.properties.getManagementTags())
				.containsOnly(ConsulDiscoveryProperties.MANAGEMENT, "newTag");
	}

}
