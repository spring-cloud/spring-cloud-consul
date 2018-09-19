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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

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

/**
 * @author Aleksandr Tarasov (aatarasov)
 * @author Lomesh Patel (lomeshpatel)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationManagementServiceTests.TestConfig.class, properties = {
		"spring.application.name=myTestService-EE",
		"spring.cloud.consul.discovery.registerHealthCheck=false",
		"management.server.port=4452" }, webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationManagementServiceTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		final Response<Map<String, Service>> response = consul.getAgentServices();
		final Map<String, Service> services = response.getValue();

		final Service service = services.get("myTestService-EE-0");
		assertNotNull("service was null", service);
		assertNotEquals("service port was 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService-EE-0", service.getId());
		assertEquals("service name was wrong", "myTestService-EE", service.getService());
		assertFalse("service address must not be empty",
				StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties",
				discoveryProperties.getHostname(), service.getAddress());

		final Service managementService = services.get("myTestService-EE-0-management");
		assertNotNull("management service was null", managementService);
		assertEquals("management service port was wrong", 4452,
				managementService.getPort().intValue());
		assertEquals("management service id was wrong", "myTestService-EE-0-management",
				managementService.getId());
		assertEquals("management service name was wrong", "myTestService-EE-management",
				managementService.getService());
		assertFalse("management service address must not be empty",
				StringUtils.isEmpty(managementService.getAddress()));
		assertEquals(
				"management service address must equals hostname from discovery properties",
				discoveryProperties.getHostname(), managementService.getAddress());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {
	}
}
