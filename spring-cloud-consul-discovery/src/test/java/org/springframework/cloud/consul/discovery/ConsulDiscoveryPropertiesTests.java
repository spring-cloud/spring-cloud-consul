package org.springframework.cloud.consul.discovery;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConsulDiscoveryPropertiesTests {

    private static final String DEFAULT_TAG = "defaultTag";
    private static final String MAP_TAG = "mapTag";
    private static final String MAP_DC = "mapDc";
    private static final String SERVICE_NAME_IN_MAP = "serviceNameInMap";
    private static final String SERVICE_NAME_NOT_IN_MAP = "serviceNameNotInMap";
    private ConsulDiscoveryProperties properties;
    private Map<String, String> serverListQueryTags = Collections.singletonMap(SERVICE_NAME_IN_MAP, MAP_TAG);
    private Map<String, String> datacenters = Collections.singletonMap(SERVICE_NAME_IN_MAP, MAP_DC);

    @Before
    public void setUp() throws Exception {
        properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
        properties.setDefaultQueryTag(DEFAULT_TAG);
        properties.setServerListQueryTags(serverListQueryTags);
        properties.setDatacenters(datacenters);
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

    @Test
    public void testGetDcReturnsNullWhenNotInMap() throws Exception {
        assertNull(properties.getDatacenters().get(SERVICE_NAME_NOT_IN_MAP));
    }

    @Test
    public void testGetDcReturnsMapValueWhenInMap() throws Exception {
        assertEquals(MAP_DC, properties.getDatacenters().get(SERVICE_NAME_IN_MAP));
    }
}
