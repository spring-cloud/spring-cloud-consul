/*
 * Copyright 2013-2018 the original author or authors.
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
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 * @author Tim Ysewyn
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulDiscoveryClientCustomizedTests.MyTestConfig.class,
	properties = { "spring.application.name=testConsulDiscovery2",
		"spring.cloud.consul.discovery.instanceId=testConsulDiscovery2Id",
		"spring.cloud.consul.discovery.hostname=testConsulDiscovery2Host",
		"spring.cloud.consul.discovery.registerHealthCheck=false",
		"spring.cloud.consul.discovery.tags=plaintag,foo=bar,foo2=bar2=baz2" },
		webEnvironment = RANDOM_PORT)
public class ConsulDiscoveryClientCustomizedTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Test
	public void getInstancesForServiceWorks() {
		List<ServiceInstance> instances = discoveryClient.getInstances("consul");
		assertNotNull("instances was null", instances);
		assertFalse("instances was empty", instances.isEmpty());
	}

	private void assertNotIpAddress(ServiceInstance instance) {
		assertFalse("host is an ip address",
				InetAddressUtils.isIPv4Address(instance.getHost()));
	}

	@Test
	public void getMetadataWorks() throws InterruptedException {
		List<ServiceInstance> instances = discoveryClient
				.getInstances("testConsulDiscovery2");
		assertNotNull("instances was null", instances);
		assertFalse("instances was empty", instances.isEmpty());

		ServiceInstance instance = instances.get(0);
		assertInstance(instance);
	}

	private void assertInstance(ServiceInstance instance) {
		assertEquals("instance id was wrong", "testConsulDiscovery2Id", instance.getInstanceId());
		assertEquals("service id was wrong", "testConsulDiscovery2", instance.getServiceId());

		Map<String, String> metadata = instance.getMetadata();
		assertNotNull("metadata was null", metadata);

		String foo = metadata.get("foo");
		assertEquals("metadata key foo was wrong", "bar", foo);

		String plaintag = metadata.get("plaintag");
		assertEquals("metadata key plaintag was wrong", "plaintag", plaintag);

		String foo2 = metadata.get("foo2");
		assertEquals("metadata key foo2 was wrong", "bar2=baz2", foo2);
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}
}
