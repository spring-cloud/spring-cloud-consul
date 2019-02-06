/*
 * Copyright 2013-2017 the original author or authors.
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

import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexey Savchuk
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulServiceRegistryCheckTtlTests.TestConfig.class, properties = {
		"spring.application.name=myTestService-S",
		"spring.cloud.consul.discovery.heartbeat.enabled=true"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsulServiceRegistryCheckTtlTests {

	@LocalServerPort
	private int randomServerPort;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Autowired
	private ConsulServiceRegistry consulServiceRegistry;

	@Autowired
	private ConsulRegistration registration;

	@Autowired
	private TtlScheduler ttlScheduler;

	private ConsulRegistration createHttpRegistration() {
		NewService service = registration.getService();
		NewService.Check httpCheck = new NewService.Check();
		httpCheck.setHttp(String.format(
				"%s://%s:%s%s",
				discoveryProperties.getScheme(),
				discoveryProperties.getHostname(),
				randomServerPort,
				discoveryProperties.getHealthCheckPath()
		));
		httpCheck.setInterval(discoveryProperties.getHealthCheckInterval());
		NewService httpService = new NewService();
		httpService.setId(service.getId() + "-http");
		httpService.setName(service.getName() + "-http");
		httpService.setCheck(httpCheck);
		return new ConsulRegistration(httpService, discoveryProperties);
	}

	@Test
	public void contextLoads() throws NoSuchFieldException, IllegalAccessException {
		ConsulRegistration httpRegistration = createHttpRegistration();
		consulServiceRegistry.register(httpRegistration);
		Field serviceHeartbeatsField = TtlScheduler.class.getDeclaredField("serviceHeartbeats");
		serviceHeartbeatsField.setAccessible(true);
		Map serviceHeartbeats = (Map) serviceHeartbeatsField.get(ttlScheduler);
		assertTrue("Service with heartbeat check not registered in TTL scheduler", serviceHeartbeats.keySet().contains(registration.getInstanceId()));
		assertFalse("Service with HTTP check registered in TTL scheduler", serviceHeartbeats.keySet().contains(httpRegistration.getInstanceId()));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class, ConsulAutoServiceRegistrationAutoConfiguration.class})
	protected static class TestConfig {
	}

}
