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
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Dmitry Zhikharev (jihor)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = ConsulAutoServiceRegistrationManagementDisabledServiceTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-NM",
				"spring.cloud.consul.discovery.instanceId=myTestService1-NM",
				"spring.cloud.service-registry.auto-registration.register-management=false",
				"spring.cloud.consul.discovery.managementPort=4453",
				"management.port=0" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationManagementDisabledServiceTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();

		Service mgmtService = services.get("myTestService-NM-0-management");
		assertThat(mgmtService).as("Management service was not null").isNull();

		Service service = services.get("myTestService1-NM");
		assertThat(service).as("Service was not null").isNotNull();
		assertThat(service.getPort().intValue()).as("service port was 0").isNotEqualTo(0);
		assertThat(service.getId()).as("service id was wrong")
				.isEqualTo("myTestService1-NM");
		assertThat(service.getService()).as("service name was wrong")
				.isEqualTo("myTestService-NM");
		assertThat(StringUtils.isEmpty(service.getAddress()))
				.as("service address must not be empty").isFalse();
		assertThat(service.getAddress())
				.as("service address must equals hostname from discovery properties")
				.isEqualTo(this.discoveryProperties.getHostname());

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
