package org.springframework.cloud.consul.discovery;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConsulDiscoveryPropertiesTest {

    final String DEFAULT_TAG = "defaultTag";
    final String MAP_TAG = "mapTag";
    final String SERVICE_NAME_IN_MAP = "serviceNameInMap";
    final String SERVICE_NAME_NOT_IN_MAP = "serviceNameNotInMap";
    ConsulDiscoveryProperties properties;
    Map<String, String> serverListQueryTags = ImmutableMap.of(SERVICE_NAME_IN_MAP, MAP_TAG);

    @Before
    public void setUp() throws Exception {
        properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
        properties.setDefaultQueryTag(DEFAULT_TAG);
        properties.setServerListQueryTags(serverListQueryTags);
    }

    @Test
    public void testReturnsNullWhenNoDefaultAndNotInMap() throws Exception {
        properties.setDefaultQueryTag(null);

        assertNull(properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP));
    }

    @Test
    public void testGetTagReturnsDefaultWhenNotInMap() throws Exception {
        assertEquals(DEFAULT_TAG, properties.getQueryTagForService(SERVICE_NAME_NOT_IN_MAP));
    }

    @Test
    public void testGetTagReturnsMapValueWhenInMap() throws Exception {
        assertEquals(MAP_TAG, properties.getQueryTagForService(SERVICE_NAME_IN_MAP));
    }
}