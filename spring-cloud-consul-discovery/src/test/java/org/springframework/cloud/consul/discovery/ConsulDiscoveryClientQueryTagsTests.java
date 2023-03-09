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

import java.util.Arrays;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lightweight integration tests to verify Consul query tags usage in
 * {@link ConsulDiscoveryClient#getInstances(String)}.
 *
 * @author Piotr Wielgolaski
 * @author Chris Bono
 */
class ConsulDiscoveryClientQueryTagsTests {

	private static final String NAME = "query-tags-test-services";
	static NewService QA_WEST_SERVICE = serviceForEnvironmentAndRegion("qa", "us-west", 9080);
	static NewService QA_EAST_SERVICE = serviceForEnvironmentAndRegion("qa", "us-east", 9080);
	static NewService PROD_WEST_SERVICE = serviceForEnvironmentAndRegion("prod", "us-west", 9082);
	static NewService PROD_EAST_SERVICE = serviceForEnvironmentAndRegion("prod", "us-east", 9082);

	private ApplicationContextRunner appContextRunner = new ApplicationContextRunner()
			.withInitializer(new ConsulTestcontainers()).withConfiguration(AutoConfigurations.of(TestConfig.class))
			.withPropertyValues("spring.application.name=consulServiceQueryTags",
					"spring.cloud.consul.discovery.catalogServicesWatch.enabled=false");

	private static NewService serviceForEnvironmentAndRegion(String env, String region, int port) {
		NewService service = new NewService();
		service.setAddress("localhost");
		service.setId(String.format("%s-%s-%s", NAME, env, region));
		service.setName(NAME);
		service.setPort(port);
		service.setTags(Arrays.asList(env, region));
		return service;
	}

	@Test
	void singleMatchingTagSpecifiedOnDefaultQueryTagProperty() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa").run(
				context -> assertThatGetInstancesReturnsExpectedServices(context, QA_WEST_SERVICE, QA_EAST_SERVICE));
	}

	@Test
	void singleNonMatchingTagSpecifiedOnDefaultQueryTagProperty() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=foo")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, new NewService[0]));
	}

	@Test
	void multipleMatchingTagsSpecifiedOnDefaultQueryTagProperty() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=prod,us-west")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, PROD_WEST_SERVICE));
	}

	@Test
	void multipleNonMatchingTagsSpecifiedOnDefaultQueryTagProperty() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=prod,foo")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, new NewService[0]));
	}

	@Test
	void multipleConflictingMatchingTagsSpecifiedOnDefaultQueryTagsProperty() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=prod,qa")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context));
	}

	@Test
	void emptyTagSpecifiedOnDefaultQueryTagProperty() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, QA_WEST_SERVICE, QA_EAST_SERVICE,
						PROD_WEST_SERVICE, PROD_EAST_SERVICE));
	}

	@Test
	void singleTagSpecifiedOnServerListQueryTagsProperty() {
		appContextRunner
				.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa",
						"spring.cloud.consul.discovery.server-list-query-tags[" + NAME + "]=prod")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, PROD_WEST_SERVICE,
						PROD_EAST_SERVICE));
	}

	@Test
	void singleNonMatchingTagSpecifiedOnServerListQueryTagsProperty() {
		appContextRunner
				.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa",
						"spring.cloud.consul.discovery.server-list-query-tags[" + NAME + "]=foo")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, new NewService[0]));
	}

	@Test
	void multipleMatchingTagsSpecifiedOnServerListQueryTagsProperty() {
		appContextRunner
				.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa",
						"spring.cloud.consul.discovery.server-list-query-tags[" + NAME + "]=prod,us-west")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, PROD_WEST_SERVICE));
	}

	@Test
	void multipleNotAllMatchingTagsSpecifiedOnServerListQueryTagsProperty() {
		appContextRunner
				.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa",
						"spring.cloud.consul.discovery.server-list-query-tags[" + NAME + "]=prod,foo")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, new NewService[0]));
	}

	@Test
	void multipleConflictingMatchingTagsSpecifiedOnServerListQueryTagsProperty() {
		appContextRunner
				.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa",
						"spring.cloud.consul.discovery.server-list-query-tags[" + NAME + "]=prod,qa")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, new NewService[0]));
	}

	@Test
	void emptyTagSpecifiedOnServerListQueryTagsProperty() {
		appContextRunner
				.withPropertyValues("spring.cloud.consul.discovery.default-query-tag=qa",
						"spring.cloud.consul.discovery.server-list-query-tags[" + NAME + "]=")
				.run(context -> assertThatGetInstancesReturnsExpectedServices(context, QA_WEST_SERVICE, QA_EAST_SERVICE,
						PROD_WEST_SERVICE, PROD_EAST_SERVICE));
	}

	@Test
	void noTagsSpecifiedOnAnyProperties() {
		appContextRunner.run(context -> assertThatGetInstancesReturnsExpectedServices(context, QA_WEST_SERVICE,
				QA_EAST_SERVICE, PROD_WEST_SERVICE, PROD_EAST_SERVICE));
	}

	private void assertThatGetInstancesReturnsExpectedServices(AssertableApplicationContext context,
			NewService... expectedServices) {
		if (expectedServices == null) {
			expectedServices = new NewService[0];
		}
		assertThat(context).hasNotFailed();
		ConsulDiscoveryClient consulDiscoveryClient = context.getBean(ConsulDiscoveryClient.class);
		List<ServiceInstance> serviceInstances = consulDiscoveryClient.getInstances(NAME);
		assertThat(serviceInstances).hasSize(expectedServices.length)
				.hasOnlyElementsOfType(ConsulServiceInstance.class);
		for (NewService expectedService : expectedServices) {
			assertThat(serviceInstances)
					.anySatisfy(serviceInstance -> assertThatServicesMatch((ConsulServiceInstance) serviceInstance,
							expectedService));
		}
	}

	private void assertThatServicesMatch(ConsulServiceInstance serviceInstance, NewService expectedService) {
		assertThat(serviceInstance.getPort()).isEqualTo(expectedService.getPort());
		assertThat(serviceInstance.getServiceId()).isEqualTo(expectedService.getName());
		assertThat(serviceInstance.getInstanceId()).isEqualTo(expectedService.getId());
		assertThat(serviceInstance).isInstanceOf(ConsulServiceInstance.class);
		assertThat(serviceInstance.getTags()).containsExactlyElementsOf(expectedService.getTags());
		assertThat(serviceInstance.getHealthService()).isNotNull();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@Import(ConsulDiscoveryClientConfiguration.class)
	protected static class TestConfig {

		@Autowired
		private ConsulClient consulClient;

		@PostConstruct
		void init() {
			consulClient.agentServiceRegister(ConsulDiscoveryClientQueryTagsTests.QA_WEST_SERVICE);
			consulClient.agentServiceRegister(ConsulDiscoveryClientQueryTagsTests.QA_EAST_SERVICE);
			consulClient.agentServiceRegister(ConsulDiscoveryClientQueryTagsTests.PROD_WEST_SERVICE);
			consulClient.agentServiceRegister(ConsulDiscoveryClientQueryTagsTests.PROD_EAST_SERVICE);
		}

		@PreDestroy
		void destroy() {
			consulClient.agentServiceDeregister(ConsulDiscoveryClientQueryTagsTests.QA_WEST_SERVICE.getId());
			consulClient.agentServiceDeregister(ConsulDiscoveryClientQueryTagsTests.QA_EAST_SERVICE.getId());
			consulClient.agentServiceDeregister(ConsulDiscoveryClientQueryTagsTests.PROD_WEST_SERVICE.getId());
			consulClient.agentServiceDeregister(ConsulDiscoveryClientQueryTagsTests.PROD_EAST_SERVICE.getId());
		}

	}

}
