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
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
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
 * @author Alex Antonov (aantonov)
 * @author Lomesh Patel (lomeshpatel)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationCustomizedManagementServicePortTests.TestConfig.class, properties = {
		"spring.application.name=myTestService-GG",
		"spring.cloud.consul.discovery.instanceId=myTestService1-GG",
		"spring.cloud.consul.discovery.registerHealthCheck=false",
		"spring.cloud.consul.discovery.managementPort=4452",
		"spring.cloud.consul.discovery.serviceName=myprefix-${spring.application.name}",
		"management.server.port=0" }, webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationCustomizedManagementServicePortTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Autowired
	private ManagementServerProperties managementServerProperties;

	@Test
	public void contextLoads() {
		final Response<Map<String, Service>> response = consul.getAgentServices();
		final Map<String, Service> services = response.getValue();

		final Service service = services.get("myTestService1-GG");
		assertNotNull("service was null", service);
		assertNotEquals("service port was 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService1-GG", service.getId());
		assertEquals("service name was wrong", "myprefix-myTestService-GG",
				service.getService());
		assertFalse("service address must not be empty",
				StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties",
				discoveryProperties.getHostname(), service.getAddress());

		final Service managementService = services.get("myTestService1-GG-management");
		assertNotNull("management service was null", managementService);
		assertEquals("management service port is not 4452", 4452,
				managementService.getPort().intValue());
		assertEquals("management port is not 0", 0,
				managementServerProperties.getPort().intValue());
		assertEquals("management service id was wrong", "myTestService1-GG-management",
				managementService.getId());
		assertEquals("management service name was wrong",
				"myprefix-myTestService-GG-management", managementService.getService());
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
