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

import static org.assertj.core.api.Assertions.assertThat;
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
		List<ServiceInstance> instances = this.discoveryClient.getInstances("consul");
		assertThat(instances).as("instances was null").isNotNull();
		assertThat(instances.isEmpty()).as("instances was empty").isFalse();
	}

	private void assertNotIpAddress(ServiceInstance instance) {
		assertThat(InetAddressUtils.isIPv4Address(instance.getHost()))
				.as("host is an ip address").isFalse();
	}

	@Test
	public void getMetadataWorks() throws InterruptedException {
		List<ServiceInstance> instances = this.discoveryClient
				.getInstances("testConsulDiscovery2");
		assertThat(instances).as("instances was null").isNotNull();
		assertThat(instances.isEmpty()).as("instances was empty").isFalse();

		ServiceInstance instance = instances.get(0);
		assertInstance(instance);
	}

	private void assertInstance(ServiceInstance instance) {
		assertThat(instance.getInstanceId()).as("instance id was wrong")
				.isEqualTo("testConsulDiscovery2Id");
		assertThat(instance.getServiceId()).as("service id was wrong")
				.isEqualTo("testConsulDiscovery2");

		Map<String, String> metadata = instance.getMetadata();
		assertThat(metadata).as("metadata was null").isNotNull();

		String foo = metadata.get("foo");
		assertThat(foo).as("metadata key foo was wrong").isEqualTo("bar");

		String plaintag = metadata.get("plaintag");
		assertThat(plaintag).as("metadata key plaintag was wrong").isEqualTo("plaintag");

		String foo2 = metadata.get("foo2");
		assertThat(foo2).as("metadata key foo2 was wrong").isEqualTo("bar2=baz2");
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}

}
