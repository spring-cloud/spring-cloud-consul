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
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationDisabledTests.TestConfig.class,
		properties = { "spring.application.name=myTestNotRegisteredService2",
				"spring.cloud.service-registry.auto-registration.enabled=false" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationDisabledTests {

	@Autowired
	private ConsulClient consul;

	@Autowired(required = false)
	private ConsulAutoServiceRegistration autoServiceRegistration;

	@Test
	public void contextLoads() {
		assertThat(this.autoServiceRegistration)
				.as("ConsulAutoServiceRegistration was created").isNull();

		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestNotRegisteredService2");
		assertThat(service).as("service was registered").isNull();
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
