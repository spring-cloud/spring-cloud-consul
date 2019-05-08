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
@SpringBootTest(
		classes = ConsulAutoServiceRegistrationCustomizedServiceNameTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-CC",
				"spring.cloud.consul.discovery.instanceId=myTestService1-CC",
				"spring.cloud.consul.discovery.serviceName=myprefix-${spring.application.name}" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationCustomizedServiceNameTests {

	@Autowired
	private ConsulClient consul;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-CC");
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort().intValue()).as("service port is 0").isNotEqualTo(0);
		assertThat(service.getId()).as("service id was wrong")
				.isEqualTo("myTestService1-CC");
		assertThat(service.getService()).as("service name was wrong")
				.isEqualTo("myprefix-myTestService-CC");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
