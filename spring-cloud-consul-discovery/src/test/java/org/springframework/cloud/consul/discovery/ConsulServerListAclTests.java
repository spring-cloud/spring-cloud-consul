/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.netflix.client.config.DefaultClientConfigImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author bomee
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulServerListAclTests.TestConfig.class,
        properties = {"spring.application.name=testConsulServerListAcl",
                "spring.cloud.consul.discovery.preferIpAddress=true",
                "consul.token=2d2e6b3b-1c82-40ab-8171-54609d8ad304"},
        webEnvironment = RANDOM_PORT)
public class ConsulServerListAclTests {

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private ConsulDiscoveryProperties properties;

    @Test
    public void test() {
        ConsulServerList consulServerList = new ConsulServerList(consulClient, properties);
        DefaultClientConfigImpl config = new DefaultClientConfigImpl();
        config.setClientName("testConsulServerListAcl");
        consulServerList.initWithNiwsConfig(config);
        List<ConsulServer> servers = consulServerList.getUpdatedListOfServers();
        assertNotNull("servers was null", servers);
        assertFalse("servers was empty", servers.isEmpty());
    }

    @Test
    public void testWithoutAclToken() {
        ConsulDiscoveryProperties noAclProperties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
        ConsulServerList consulServerList = new ConsulServerList(consulClient, noAclProperties);
        DefaultClientConfigImpl config = new DefaultClientConfigImpl();
        config.setClientName("testConsulServerListAcl");
        consulServerList.initWithNiwsConfig(config);
        List<ConsulServer> servers = consulServerList.getUpdatedListOfServers();
        assertNotNull("servers was null", servers);
        assertTrue("servers must be empty", servers.isEmpty());
    }

    @Configuration
    @EnableDiscoveryClient
    @EnableAutoConfiguration
    @Import({ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class})
    public static class TestConfig {

    }
}
