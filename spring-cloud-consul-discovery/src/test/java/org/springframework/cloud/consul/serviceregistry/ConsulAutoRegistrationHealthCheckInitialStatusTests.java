/*
 * Copyright 2013-present the original author or authors.
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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.model.http.health.Check.CheckStatus;
import org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Verifies that Consul applies the configured initial status to the registered check. A
 * TTL check is used without the heartbeat scheduler, so nothing but the initial status
 * can change the status of the check.
 *
 * @author Asish Kumar
 */
@SpringBootTest(classes = ConsulAutoRegistrationHealthCheckInitialStatusTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-HealthCheckInitialStatus",
				"spring.cloud.consul.discovery.heartbeat.enabled=true",
				"spring.cloud.consul.discovery.health-check-initial-status=warning" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoRegistrationHealthCheckInitialStatusTests {

	@Autowired
	private ConsulAutoRegistration registration;

	@Autowired
	private ConsulClient consulClient;

	@Test
	public void initialStatusIsAppliedToRegisteredCheck() {
		NewService service = this.registration.getService();
		NewService.Check check = service.getCheck();
		assertThat(check).as("check was null").isNotNull();
		assertThat(check.getTtl()).as("check was not a TTL check").isNotNull();
		assertThat(check.getStatus()).isEqualTo("warning");

		List<Check> checks = this.consulClient.getHealthChecksForService(service.getName()).getBody();
		assertThat(checks).filteredOn(c -> service.getId().equals(c.getServiceId()))
			.singleElement()
			.extracting(Check::getStatus)
			.isEqualTo(CheckStatus.WARNING);
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration(exclude = ConsulHeartbeatAutoConfiguration.class)
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
