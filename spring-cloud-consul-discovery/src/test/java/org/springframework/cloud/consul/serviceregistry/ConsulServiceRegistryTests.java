/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.agent.model.NewService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.consul.discovery.query-passing=true",
		webEnvironment = RANDOM_PORT)
public class ConsulServiceRegistryTests {

	@Autowired(required = false)
	private ConsulRegistration autoRegistration;

	@Autowired
	private ConsulServiceRegistry serviceRegistry;

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@LocalServerPort
	private int port;

	@Test
	public void contextLoads() {

		assertThat(autoRegistration).as("autoRegistration created erroneously").isNull();

		String serviceId = "myNonAutoRegisteredService";

		NewService service = new NewService();
		service.setAddress("localhost");
		service.setId("myNonAutoRegisteredService-A1");
		service.setName(serviceId);
		service.setPort(port);
		service.setTags(Collections.singletonList("mytag"));

		ConsulRegistration registration = new ConsulRegistration(service, this.properties);
		Throwable t = null;
		try {
			serviceRegistry.register(registration);
			assertHasInstance(serviceId);

			assertStatus(registration, "UP");

			// only works if query-passing = true
			serviceRegistry.setStatus(registration, "OUT_OF_SERVICE");
			assertEmptyInstances(serviceId);
			assertStatus(registration, "OUT_OF_SERVICE");

			serviceRegistry.setStatus(registration, "UP");
			assertHasInstance(serviceId);
		} catch (RuntimeException e) {
			throw e	;
		} finally {
			serviceRegistry.deregister(registration);
			if (t == null) { // just deregister, test already failed
				assertEmptyInstances(serviceId);
			}
		}

	}

	private void assertStatus(ConsulRegistration registration, String status) {
		Object o = serviceRegistry.getStatus(registration);
		assertThat(o).isEqualTo(status);
	}

	private void assertHasInstance(String serviceId) {
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
		assertThat(instances).hasSize(1);

		ServiceInstance instance = instances.get(0);
		assertThat(instance.getServiceId()).isEqualTo(serviceId);
	}

	private void assertEmptyInstances(String serviceId) {
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
		assertThat(instances).isEmpty();
	}

	@EnableDiscoveryClient(autoRegister = false)
	@SpringBootConfiguration
	@EnableAutoConfiguration
	protected static class TestConfig { }
}

