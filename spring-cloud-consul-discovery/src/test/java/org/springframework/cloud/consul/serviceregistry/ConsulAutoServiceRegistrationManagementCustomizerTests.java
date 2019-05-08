/*
 * Copyright 2013-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Alexey Savchuk (devpreview)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		ConsulAutoServiceRegistrationManagementCustomizerTests.TestConfig.class,
		ConsulAutoServiceRegistrationManagementCustomizerTests.ManagementConfig.class },
		properties = { "spring.application.name=myTestService-SS",
				"spring.cloud.consul.discovery.registerHealthCheck=false",
				"management.server.port=4453" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationManagementCustomizerTests {

	@Autowired
	private ConsulRegistration registration;

	@Autowired
	private ConsulAutoRegistration autoRegistration;

	@Test
	public void contextLoads() {
		ConsulAutoRegistration managementRegistration = this.autoRegistration
				.managementRegistration();
		List<NewService.Check> checks = managementRegistration.getService().getChecks();
		List<String> ttls = checks.stream().map(NewService.Check::getTtl)
				.collect(Collectors.toList());
		assertThat(ttls.contains("39s"))
				.as("Management registration not customized with 'foo' customizer")
				.isTrue();
		assertThat(ttls.contains("36s"))
				.as("Management registration not customized with 'bar' customizer")
				.isTrue();
	}

	@Configuration
	public static class ManagementConfig {

		@Bean
		public ConsulManagementRegistrationCustomizer fooManagementCustomizer() {
			return managementRegistration -> {
				addCheck(managementRegistration, "39s");
			};
		}

		@Bean
		public ConsulManagementRegistrationCustomizer barManagementCustomizer() {
			return managementRegistration -> {
				addCheck(managementRegistration, "36s");
			};
		}

		private void addCheck(ConsulRegistration managementRegistration, String ttl) {
			NewService managementService = managementRegistration.getService();
			NewService.Check check = new NewService.Check();
			check.setTtl(ttl);
			List<NewService.Check> checks = managementService.getChecks() != null
					? new ArrayList<>(managementService.getChecks()) : new ArrayList<>();
			checks.add(check);
			managementRegistration.getService().setChecks(checks);
		}

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
