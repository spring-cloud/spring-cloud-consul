package org.springframework.cloud.consul.discovery;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConsulDiscoveryPropertiesTests {

	private static final String DEFAULT_TAG = "defaultTag";
	private static final String MAP_TAG = "mapTag";
	private static final String MAP_DC = "mapDc";
	private static final String SERVICE_NAME_IN_MAP = "serviceNameInMap";
	private static final String SERVICE_NAME_NOT_IN_MAP = "serviceNameNotInMap";
	private ConsulDiscoveryProperties properties;
	private final Map<String, String> serverListQueryTags = Collections.singletonMap(SERVICE_NAME_IN_MAP, MAP_TAG);
	private final Map<String, String> datacenters = Collections.singletonMap(SERVICE_NAME_IN_MAP, MAP_DC);

	@Before
	public void setUp() {
		properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		properties.setDefaultQueryTag(DEFAULT_TAG);
		properties.setServerListQueryTags(serverListQueryTags);
		properties.setDatacenters(datacenters);
	}

	@Test
	public void testReturnsNullWhenNoDefaultAndNotInMap() {
		properties.setDefaultQueryTag(null);

		assertNull(properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP));
	}

	@Test
	public void testGetTagReturnsDefaultWhenNotInMap() {
		assertEquals(DEFAULT_TAG, properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP));
	}

	@Test
	public void testGetTagReturnsMapValueWhenInMap() {
		assertEquals(MAP_TAG, properties.getQueryTagForService(SERVICE_NAME_IN_MAP));
	}

	@Test
	public void testGetDcReturnsNullWhenNotInMap() {
		assertNull(properties.getDatacenters().get(SERVICE_NAME_NOT_IN_MAP));
	}

	@Test
	public void testGetDcReturnsMapValueWhenInMap() {
		assertEquals(MAP_DC, properties.getDatacenters().get(SERVICE_NAME_IN_MAP));
	}

	@Test
	public void testAddManagementTag() {
		properties.getManagementTags().add("newTag");
		assertThat(properties.getManagementTags())
				.containsOnly(ConsulDiscoveryProperties.MANAGEMENT, "newTag");
	}
}
