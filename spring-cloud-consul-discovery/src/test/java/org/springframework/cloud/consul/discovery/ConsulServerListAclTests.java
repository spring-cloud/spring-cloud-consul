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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.netflix.client.config.DefaultClientConfigImpl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    public void serverListWorksWithAcl() {
        ConsulServerList consulServerList = new ConsulServerList(consulClient, properties);
        DefaultClientConfigImpl config = new DefaultClientConfigImpl();
        config.setClientName("testConsulServerListAcl");
        consulServerList.initWithNiwsConfig(config);
        List<ConsulServer> servers = consulServerList.getUpdatedListOfServers();
        assertNotNull("servers was null", servers);
        assertFalse("servers was empty", servers.isEmpty());
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableDiscoveryClient
    public static class TestConfig {

    }
}
