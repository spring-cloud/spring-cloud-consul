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

import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Niko Tung
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = ConsulAutoRegistrationCheckTtlDeregisterCriticalServiceTests.TestConfig.class,
		properties = {
				"spring.application.name=myConsulServiceRegistryHealthCheckTtlDeregisterCriticalServiceAfter-N",
				"spring.cloud.consul.discovery.health-check-critical-timeout=1m",
				"spring.cloud.consul.discovery.heartbeat.enabled=true" },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsulAutoRegistrationCheckTtlDeregisterCriticalServiceTests {

	@Autowired
	private ConsulRegistration registration;

	@Test
	public void contextLoads() {
		NewService service = registration.getService();
		assertThat("1m".equals(service.getCheck().getDeregisterCriticalServiceAfter()))
				.as("Service with heartbeat check and deregister critical timeout registered")
				.isTrue();
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class,
			ConsulHeartbeatAutoConfiguration.class })
	protected static class TestConfig {

	}

}
