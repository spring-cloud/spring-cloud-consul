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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=testConsulLoadBalancer",
		"spring.cloud.consul.discovery.prefer-ip-address=true", "spring.cloud.consul.discovery.metadata.foo=bar" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulLoadbalancerClientTests {

	@Autowired
	private LoadBalancerClient client;

	@Test
	public void chooseWorks() {
		ServiceInstance instance = this.client.choose("testConsulLoadBalancer");
		assertThat(instance).isNotNull();

		assertThat(instance.isSecure()).isFalse();
		assertIpAddress(instance);
		assertThat(instance.getMetadata()).containsEntry("foo", "bar");
	}

	private void assertIpAddress(ServiceInstance instance) {
		assertThat(Character.isDigit(instance.getHost().charAt(0))).as("host isn't an ip address").isTrue();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}

}
