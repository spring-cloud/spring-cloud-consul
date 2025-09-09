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
import java.util.concurrent.TimeUnit;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test that verifies the TTL checks when the
 * {@link ApplicationStatusProvider} is an
 * {@link ActuatorHealthApplicationStatusProvider}.
 *
 * @author Chris Bono
 */
@SpringBootTest(classes = ConsulAutoServiceRegistrationTtlCheckWithActuatorHealthTests.TestConfig.class, properties = {
		"spring.application.name=" + ConsulAutoServiceRegistrationTtlCheckWithActuatorHealthTests.SERVICE_NAME,
		"spring.cloud.consul.discovery.heartbeat.enabled=true", "spring.cloud.consul.discovery.heartbeat.ttl=2s" },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoServiceRegistrationTtlCheckWithActuatorHealthTests {

	// Visible for constant in SpringBootTest.properties
	static final String SERVICE_NAME = "ttl-check-actuator-health-test-svc";

	@Autowired
	private ConsulClient consul;

	@Autowired
	private StaticHealthIndicator ttlStatusControllingIndicator;

	@Test
	void serviceRegisteredWithApplicationHealthRespectingTtlCheck() {

		switchHealthIndicatorStatusTo(Status.UP);
		assertThatConsulTtlCheckIsInStatus(PASSING);

		switchHealthIndicatorStatusTo(Status.DOWN);
		assertThatConsulTtlCheckIsInStatus(CRITICAL);

		switchHealthIndicatorStatusTo(Status.UP);
		assertThatConsulTtlCheckIsInStatus(PASSING);

		switchHealthIndicatorStatusTo(Status.OUT_OF_SERVICE);
		assertThatConsulTtlCheckIsInStatus(CRITICAL);

		switchHealthIndicatorStatusTo(Status.UP);
		assertThatConsulTtlCheckIsInStatus(PASSING);
	}

	private void switchHealthIndicatorStatusTo(Status newStatus) {
		ttlStatusControllingIndicator.setStatus(newStatus);
	}

	private void assertThatConsulTtlCheckIsInStatus(Check.CheckStatus expectedStatus) {
		await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> assertThat(getCheckForService(SERVICE_NAME)).isNotNull()
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
		StaticHealthIndicator ttlStatusControllingIndicator() {
			return new StaticHealthIndicator();
		}

	}

	static class StaticHealthIndicator implements HealthIndicator {

		private Status status = Status.UP;

		void setStatus(Status status) {
			this.status = status;
		}

		@Override
		public Health health() {
			return Health.status(status).build();
		}

	}

}
