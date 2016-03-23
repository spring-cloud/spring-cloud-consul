/*
 * Copyright 2013-2015 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ConsulDiscoveryClientCustomizedTests.MyTestConfig.class)
@WebIntegrationTest(value = { "spring.application.name=testConsulDiscovery2",
		"spring.cloud.consul.discovery.instanceId=testConsulDiscovery2Id" }, randomPort = true)
public class ConsulDiscoveryClientCustomizedTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private ConsulClient consulClient;

	@Before
	public void registerExcludedServices() {
		NewService excluded = new NewService();
		excluded.setAddress("www.google.com");
		excluded.setPort(80);
		excluded.setName("excluded");
		excluded.setTags(Arrays.asList("excluded"));

		NewService included = new NewService();
		included.setAddress("www.google.com");
		included.setPort(443);
		included.setName("included");
		included.setTags(Arrays.asList("included"));

		consulClient.agentServiceRegister(excluded);
		consulClient.agentServiceRegister(included);
	}

	@After
	public void deregisterExcludedServices() {
		Response<Map<String, Service>> agentServices = consulClient.getAgentServices();
		for (Map.Entry<String, Service> serviceEntry : agentServices.getValue()
				.entrySet()) {
			if (serviceEntry.getValue().getService().equals("included")) {
				consulClient.agentServiceDeregister(serviceEntry.getKey());
			}
			if (serviceEntry.getValue().getService().equals("excluded")) {
				consulClient.agentServiceDeregister(serviceEntry.getKey());
			}
		}
	}

	@Test
	public void getInstancesForServiceWorks() {
		List<ServiceInstance> instances = discoveryClient.getInstances("consul");
		assertNotNull("instances was null", instances);
		assertFalse("instances was empty", instances.isEmpty());
	}

	private void assertNotIpAddress(final ServiceInstance instance) {
		assertFalse("host is an ip address",
				InetAddressUtils.isIPv4Address(instance.getHost()));
	}

	@Test
	public void getLocalInstance() {
		ServiceInstance instance = discoveryClient.getLocalServiceInstance();
		assertNotNull("instance was null", instance);
		assertNotIpAddress(instance);
		assertEquals("instance id was wrong", "testConsulDiscovery2Id",
				instance.getServiceId());
	}

	@Test
	public void assertExcludedServiceEmpty() {
		List<ServiceInstance> instances = discoveryClient.getInstances("excluded");
		assertTrue(instances.isEmpty());
	}

	@Test
	public void assertIncludedServiceNonEmpty() {
		List<ServiceInstance> instances = discoveryClient.getInstances("included");
		assertFalse(instances.isEmpty());
	}

	@Configuration
	@EnableDiscoveryClient
	@EnableAutoConfiguration
	@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
	@PropertySource("classpath:/consulDiscoveryClientCustomizedTests.properties")
	public static class MyTestConfig {

	}
}
