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
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Jon Freedman
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationDisabledTests.TestConfig.class,
		properties = { "spring.application.name=myTestNotDeRegisteredService",
				"spring.cloud.consul.discovery.instanceId=myTestNotDeRegisteredService-D",
				"spring.cloud.consul.discovery.deregister=false" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceDeRegistrationDisabledTests {

	@Autowired
	private ConsulClient consul;

	@Autowired(required = false)
	private ConsulAutoServiceRegistration autoServiceRegistration;

	@Autowired(required = false)
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		assertThat(this.autoServiceRegistration)
				.as("ConsulAutoServiceRegistration was not created").isNotNull();
		assertThat(this.discoveryProperties)
				.as("ConsulDiscoveryProperties was not created").isNotNull();

		checkService(true);
		this.autoServiceRegistration.deregister();
		checkService(true);
		this.discoveryProperties.setDeregister(true);
		this.autoServiceRegistration.deregister();
		checkService(false);
	}

	private void checkService(final boolean expected) {
		final Response<Map<String, Service>> response = this.consul.getAgentServices();
		final Map<String, Service> services = response.getValue();
		final Service service = services.get("myTestNotDeRegisteredService-D");
		if (expected) {
			assertThat(service).as("service was not registered").isNotNull();
		}
		else {
			assertThat(service).as("service was registered").isNull();
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
