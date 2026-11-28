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

package org.springframework.cloud.consul.discovery.loadbalancer;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PassingChecksServiceInstanceListSupplier} with Spring
 * Cloud LoadBalancer and Consul.
 *
 * @author Asish Kumar
 */
@SpringBootTest(properties = { "spring.application.name=passingChecksLoadBalancerTest",
		"spring.cloud.consul.discovery.register=false",
		"spring.cloud.consul.discovery.catalog-services-watch.enabled=false" })
@ContextConfiguration(initializers = ConsulTestcontainers.class)
class PassingChecksServiceInstanceListSupplierIntegrationTests {

	private static final String SERVICE_NAME = "passing-checks-test-service";

	private static final String PASSING_ID = SERVICE_NAME + "-passing";

	private static final String WARNING_ID = SERVICE_NAME + "-warning";

	private static final String CRITICAL_ID = SERVICE_NAME + "-critical";

	@Autowired
	private ConsulClient consulClient;

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private LoadBalancerClientFactory loadBalancerClientFactory;

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	@BeforeEach
	void registerServices() {
		registerService(PASSING_ID, 8081);
		registerService(WARNING_ID, 8082);
		registerService(CRITICAL_ID, 8083);
		consulClient.agentCheckPass("service:" + PASSING_ID, null, null);
		consulClient.agentCheckWarn("service:" + WARNING_ID, null, null);
		consulClient.agentCheckFail("service:" + CRITICAL_ID, null, null);
	}

	@AfterEach
	void deregisterServices() {
		consulClient.agentServiceDeregister(PASSING_ID, null);
		consulClient.agentServiceDeregister(WARNING_ID, null);
		consulClient.agentServiceDeregister(CRITICAL_ID, null);
	}

	@Test
	void discoveryClientReturnsAllInstances() {
		assertThat(instanceIds(discoveryClient.getInstances(SERVICE_NAME))).containsExactlyInAnyOrder(PASSING_ID,
				WARNING_ID, CRITICAL_ID);
	}

	@Test
	void supplierOnlyReturnsInstancesWithPassingChecks() {
		ServiceInstanceListSupplier supplier = loadBalancerClientFactory.getInstance(SERVICE_NAME,
				ServiceInstanceListSupplier.class);

		assertThat(supplier).isNotNull();
		assertThat(instanceIds(supplier.get().blockFirst())).containsExactly(PASSING_ID);
	}

	@Test
	void loadBalancerOnlyChoosesInstancesWithPassingChecks() {
		for (int i = 0; i < 6; i++) {
			ServiceInstance instance = loadBalancerClient.choose(SERVICE_NAME);
			assertThat(instance).isNotNull();
			assertThat(instance.getInstanceId()).isEqualTo(PASSING_ID);
		}
	}

	@Test
	void loadBalancerChoosesNothingWhenNoInstancePassesItsChecks() {
		consulClient.agentCheckFail("service:" + PASSING_ID, null, null);

		assertThat(loadBalancerClient.choose(SERVICE_NAME)).isNull();
	}

	private void registerService(String id, int port) {
		NewService service = new NewService();
		service.setId(id);
		service.setName(SERVICE_NAME);
		service.setAddress("localhost");
		service.setPort(port);
		NewService.Check check = new NewService.Check();
		check.setTtl("10m");
		service.setCheck(check);
		consulClient.agentServiceRegister(null, service);
	}

	private static List<String> instanceIds(List<ServiceInstance> instances) {
		return instances.stream().map(ServiceInstance::getInstanceId).collect(Collectors.toList());
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@LoadBalancerClients(defaultConfiguration = PassingChecksLoadBalancerConfiguration.class)
	static class TestConfig {

	}

	static class PassingChecksLoadBalancerConfiguration {

		@Bean
		ServiceInstanceListSupplier passingChecksServiceInstanceListSupplier(ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder()
				.withBlockingDiscoveryClient()
				.with((ctx, delegate) -> new PassingChecksServiceInstanceListSupplier(delegate))
				.build(context);
		}

	}

}
