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
 * @author Glen Lockhart
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		properties = { "spring.application.name=testConsulDiscoveryHttps",
				"spring.cloud.consul.discovery.prefer-ip-address=true",
				"spring.cloud.consul.discovery.scheme=https" },
		classes = ConsulDiscoveryClientHttpsTests.MyTestConfig.class,
		webEnvironment = RANDOM_PORT)
public class ConsulDiscoveryClientHttpsTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Test
	public void getInstancesForServiceWorks() {
		List<ServiceInstance> instances = this.discoveryClient
				.getInstances("testConsulDiscoveryHttps");
		assertThat(instances).as("instances was null").isNotNull();
		assertThat(instances.isEmpty()).as("instances was empty").isFalse();

		ServiceInstance instance = instances.get(0);
		assertThat(instance.isSecure()).as("instance was not secure (https)").isTrue();
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}

}
