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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author Tomas Forsman
 */
@SpringBootTest(classes = ConsulDiscoveryClientNoPortTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-NoPort",
				"spring.cloud.consul.discovery.instanceId=myTestService1-NoPort" },
		webEnvironment = NONE)
@Testcontainers
public class ConsulDiscoveryClientNoPortTests {

	// 1.10 introduces different data for port
	@Container
	static GenericContainer<?> consul = ConsulTestcontainers.createConsulContainer("1.10");

	@DynamicPropertySource
	static void consulProperties(DynamicPropertyRegistry registry) {
		registry.add(ConsulProperties.PREFIX + ".host", consul::getContainerIpAddress);
		registry.add(ConsulProperties.PREFIX + ".port", () -> consul.getMappedPort(ConsulTestcontainers.DEFAULT_PORT));
	}

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private ConsulClient client;

	@Test
	public void contextLoads() {
		NewService newService = new NewService();
		newService.setId("myTestService2-NoPort");
		newService.setName("myTestService2-NoPort");
		client.agentServiceRegister(newService);

		List<ServiceInstance> instances = discoveryClient.getInstances("myTestService2-NoPort");
		assertThat(instances.size()).isEqualTo(1);
		ServiceInstance service = instances.get(0);
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort()).as("service port is not 0").isZero();
		assertThat(service.getInstanceId()).as("service id was wrong").isEqualTo("myTestService2-NoPort");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
