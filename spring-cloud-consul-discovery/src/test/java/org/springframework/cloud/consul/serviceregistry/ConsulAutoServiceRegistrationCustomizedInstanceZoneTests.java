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
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Sixian Liu
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationCustomizedInstanceZoneTests.TestConfig.class,
	properties = { "spring.application.name=myTestService-WithZone",
		"spring.cloud.consul.discovery.instanceId=myTestService1-WithZone",
		"spring.cloud.consul.discovery.instanceZone=zone1",
		"spring.cloud.consul.discovery.defaultZoneMetadataName=myZone"},
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationCustomizedInstanceZoneTests {

	@Autowired
	private ConsulClient consul;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-WithZone");
		assertNotNull("service was null", service);
		assertNotEquals("service port is 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService1-WithZone", service.getId());
		assertTrue("service zone was wrong", service.getTags().contains("myZone=zone1"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class, ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig { }
}
