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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.Check.CheckStatus;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test that verifies the TTL checks basic functionality.
 *
 * @author Chris Bono
 */
@SpringBootTest(classes = ConsulAutoServiceRegistrationTtlCheckTests.TestConfig.class,
		properties = { "spring.application.name=" + ConsulAutoServiceRegistrationTtlCheckTests.SERVICE_NAME,
				"spring.cloud.consul.discovery.instance-id=" + ConsulAutoServiceRegistrationTtlCheckTests.INSTANCE_ID,
				"spring.cloud.consul.discovery.heartbeat.enabled=true",
				"spring.cloud.consul.discovery.heartbeat.ttl=2s", "management.server.port=0" },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
class ConsulAutoServiceRegistrationTtlCheckTests {

	// Visible for constant in SpringBootTest.properties
	static final String SERVICE_NAME = "ttl-check-test-svc";

	// Visible for constant in SpringBootTest.properties
	static final String INSTANCE_ID = SERVICE_NAME + "-001";

	private static final String MGMT_SERVICE_NAME = SERVICE_NAME + "-management";

	@Autowired
	private ConsulClient consul;

	@Autowired
	private TtlScheduler ttlScheduler;

	@Test
	void serviceAndManagementTtlChecksRegisteredAndInPasssingStatusInitially() {

		assertThatConsulTtlCheckIsInStatus(SERVICE_NAME, PASSING);
		assertThatConsulTtlCheckIsInStatus(MGMT_SERVICE_NAME, PASSING);

		// Wait for TTL to expire and make sure it actually got renewed prior
		await().pollDelay(Duration.ofMillis(2500)).until(() -> true);

		assertThatConsulTtlCheckIsInStatus(SERVICE_NAME, PASSING);
		assertThatConsulTtlCheckIsInStatus(MGMT_SERVICE_NAME, PASSING);
	}

	@Test
	void serviceGoesIntoCriticalStatusWhenRemovedFromTheTtlScheduler() {

		assertThatConsulTtlCheckIsInStatus(SERVICE_NAME, PASSING);

		// Remove service from TtlScheduler which should quit sending updates to Consul
		this.ttlScheduler.remove(INSTANCE_ID);

		// Wait for TTL to expire and make sure it did not get renewed
		await().pollDelay(Duration.ofMillis(2500)).until(() -> true);

		assertThatConsulTtlCheckIsInStatus(SERVICE_NAME, CRITICAL);
	}

	private void assertThatConsulTtlCheckIsInStatus(String serviceName, CheckStatus expectedStatus) {
		await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> assertThat(getCheckForService(serviceName)).isNotNull()
				.extracting(Check::getStatus)
				.isEqualTo(expectedStatus));
	}

	private Check getCheckForService(String serviceName) {
		Response<List<Check>> checkResponse = this.consul.getHealthChecksForService(serviceName,
				HealthChecksForServiceRequest.newBuilder().setQueryParams(QueryParams.DEFAULT).build());
		if (checkResponse.getValue() == null || checkResponse.getValue().isEmpty()) {
			return null;
		}
		return checkResponse.getValue().get(0);
	}

	@ConsulAutoServiceRegistrationIntegrationTestConfig
	static class TestConfig {

		@Bean
		ApplicationStatusProvider alwaysPassingApplicationStatusProvider() {
			return () -> CheckStatus.PASSING;
		}

	}

}
