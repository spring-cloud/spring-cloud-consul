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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.ConsulClient.QueryParams;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 * @author Joe Athman
 */
@SpringBootTest(properties = { "spring.application.name=testConsulDiscovery",
		"spring.cloud.consul.discovery.prefer-ip-address=true", "spring.cloud.consul.discovery.metadata[foo]=bar" },
		classes = ConsulDiscoveryClientTests.MyTestConfig.class, webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulDiscoveryClientTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private ConsulClient consulClient;

	@Test
	public void getInstancesForServiceWorks() {
		List<ServiceInstance> instances = this.discoveryClient.getInstances("testConsulDiscovery");
		assertThat(instances).as("instances was null").isNotNull();
		assertThat(instances.isEmpty()).as("instances was empty").isFalse();

		ServiceInstance instance = instances.get(0);
		assertThat(instance.isSecure()).as("instance was secure (https)").isFalse();
		assertIpAddress(instance);
		assertThat(instance.getMetadata()).containsEntry("foo", "bar");
	}

	@Test
	public void getInstancesForServiceRespectsQueryParams() {
		ResponseEntity<List<String>> catalogDatacenters = this.consulClient.getCatalogDatacenters(null);

		List<String> dataCenterList = catalogDatacenters.getBody();
		assertThat(dataCenterList.isEmpty()).as("no data centers found").isFalse();
		List<ServiceInstance> instances = this.discoveryClient.getInstances("testConsulDiscovery",
				new QueryParams(dataCenterList.get(0)));
		assertThat(instances.isEmpty()).as("instances was empty").isFalse();

		ServiceInstance instance = instances.get(0);
		assertIpAddress(instance);
	}

	@Test
	public void probeWorks() {
		discoveryClient.probe();
	}

	private void assertIpAddress(ServiceInstance instance) {
		assertThat(Character.isDigit(instance.getHost().charAt(0))).as("host isn't an ip address").isTrue();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}

}
