/*
 * Copyright 2013-2016 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Marcin Biegan
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationCustomizedTests.MyTestConfig.class,
		properties = { "spring.application.name=testCustomAutoServiceRegistration"},
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationCustomizedTests {

	@Autowired
	private AutoServiceRegistrationProperties autoServiceRegistrationProperties;

	@Autowired
	private ConsulAutoServiceRegistration registration1;

	@Autowired
	private CustomAutoRegistration registration2;

	@Test
	public void usesCustomConsulLifecycle() {
		assertEquals("configuration is not customized", "customconfiguration", registration1.getConfiguration());
		assertEquals("configuration is not customized", "customconfiguration", registration2.getConfiguration());
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class, ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class MyTestConfig {
		@Bean
		public CustomAutoRegistration consulAutoServiceRegistration(
				ConsulServiceRegistry serviceRegistry,
				AutoServiceRegistrationProperties autoServiceRegistrationProperties,
				ConsulDiscoveryProperties properties,
				ConsulAutoRegistration registration) {
			return new CustomAutoRegistration(serviceRegistry,
					autoServiceRegistrationProperties, properties, registration);
		}
	}

	public static class CustomAutoRegistration extends ConsulAutoServiceRegistration {

		@Autowired
		public CustomAutoRegistration(ConsulServiceRegistry serviceRegistry,
				AutoServiceRegistrationProperties autoServiceRegistrationProperties,
				ConsulDiscoveryProperties properties,
				ConsulAutoRegistration registration) {
			super(serviceRegistry, autoServiceRegistrationProperties, properties,
					registration);
		}

		@Override
		protected Object getConfiguration() {
			return "customconfiguration";
		}
	}
}
