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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;

/**
 * @author Spencer Gibb
 * @author Joe Athman
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=testConsulDiscovery",
		"spring.cloud.consul.discovery.prefer-ip-address=true",
		"spring.cloud.consul.discovery.tags=foo=bar", },
		classes = ConsulDiscoveryClientTests.MyTestConfig.class,
		webEnvironment = RANDOM_PORT)
public class ConsulDiscoveryClientTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;
	@Autowired
	private ConsulClient consulClient;

	@Test
	public void getInstancesForServiceWorks() {
		List<ServiceInstance> instances = discoveryClient.getInstances("testConsulDiscovery");
		assertNotNull("instances was null", instances);
		assertFalse("instances was empty", instances.isEmpty());

		ServiceInstance instance = instances.get(0);
		assertFalse("instance was secure (https)", instance.isSecure());
		assertIpAddress(instance);
		assertThat(instance.getMetadata())
				.containsEntry("foo", "bar");
	}

	@Test
	public void getInstancesForServiceRespectsQueryParams() {
		Response<List<String>> catalogDatacenters = consulClient.getCatalogDatacenters();

		List<String> dataCenterList = catalogDatacenters.getValue();
		assertFalse("no data centers found", dataCenterList.isEmpty());
		List<ServiceInstance> instances = discoveryClient.getInstances("testConsulDiscovery",
				new QueryParams(dataCenterList.get(0)));
		assertFalse("instances was empty", instances.isEmpty());

		ServiceInstance instance = instances.get(0);
		assertIpAddress(instance);
	}

	private void assertIpAddress(ServiceInstance instance) {
		assertTrue("host isn't an ip address",
				Character.isDigit(instance.getHost().charAt(0)));
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}
}
