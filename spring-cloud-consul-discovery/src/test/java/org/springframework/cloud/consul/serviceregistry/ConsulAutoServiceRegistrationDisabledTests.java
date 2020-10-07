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

package org.springframework.cloud.consul.serviceregistry;

import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import org.junit.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulAutoServiceRegistrationDisabledTests {

	@Test
	public void disabledViaSpringCloudProperty() {
		testAutoRegistrationDisabled("myTestNotRegisteredService2",
				"spring.cloud.service-registry.auto-registration.enabled");
	}

	@Test
	public void disabledViaConsulProperty() {
		testAutoRegistrationDisabled("myTestNotRegisteredService3",
				"spring.cloud.consul.service-registry.auto-registration.enabled");
	}

	@Test
	public void disabledViaSpringCloudServiceRegistryProperty() {
		testAutoRegistrationDisabled("myTestNotRegisteredService4", "spring.cloud.service-registry.enabled");
	}

	@Test
	public void disabledViaConsulServiceRegistryProperty() {
		testAutoRegistrationDisabled("myTestNotRegisteredService5", "spring.cloud.consul.service-registry.enabled");
	}

	private void testAutoRegistrationDisabled(String testName, String disableProperty) {
		new WebApplicationContextRunner().withUserConfiguration(TestConfig.class)
				.withPropertyValues("spring.application.name=" + testName, disableProperty + "=false", "server.port=0")
				.withInitializer(new ConsulTestcontainers()).run(context -> {

					assertThat(context).doesNotHaveBean(ConsulAutoServiceRegistration.class);
					assertThat(context).doesNotHaveBean(ConsulAutoServiceRegistrationListener.class);
					assertThat(context).doesNotHaveBean(ConsulAutoRegistration.class);
					assertThat(context).doesNotHaveBean(ConsulRegistrationCustomizer.class);

					ConsulClient consul = context.getBean(ConsulClient.class);

					Response<Map<String, Service>> response = consul.getAgentServices();
					Map<String, Service> services = response.getValue();
					Service service = services.get(testName);
					assertThat(service).as("service was registered").isNull();

				});
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	public static class TestConfig {

	}

}
