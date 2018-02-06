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

import java.util.Map;

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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Dmitry Zhikharev (jihor)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationManagementDisabledServiceTests.TestConfig.class, properties = {
		"spring.application.name=myTestService-NM",
		"spring.cloud.consul.discovery.instanceId=myTestService1-NM",
		"spring.cloud.service-registry.auto-registration.register-management=false",
		"spring.cloud.consul.discovery.managementPort=4453",
		"management.port=0" }, webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationManagementDisabledServiceTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();

		Service mgmtService = services.get("myTestService-NM-0-management");
		assertNull("Management service was not null", mgmtService);

		Service service = services.get("myTestService1-NM");
		assertNotNull("Service was not null", service);
		assertNotEquals("service port was 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService1-NM", service.getId());
		assertEquals("service name was wrong", "myTestService-NM", service.getService());
		assertFalse("service address must not be empty",
				StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties",
				discoveryProperties.getHostname(), service.getAddress());

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {
	}
}
