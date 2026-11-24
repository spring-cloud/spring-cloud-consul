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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.health.Check.CheckStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit tests for the initial status of the check created by
 * {@link ConsulAutoRegistration#createCheck}.
 *
 * @author Asish Kumar
 */
class ConsulAutoRegistrationInitialStatusCheckTests {

	private ConsulDiscoveryProperties properties;

	private HeartbeatProperties heartbeatProperties;

	@BeforeEach
	void setUp() {
		this.properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		this.properties.setHostname("localhost");
		this.heartbeatProperties = new HeartbeatProperties();
	}

	@Test
	void initialStatusIsNotSetByDefault() {
		NewService.Check check = ConsulAutoRegistration.createCheck(8080, this.heartbeatProperties, this.properties);

		assertThat(check.getHttp()).isEqualTo("http://localhost:8080/actuator/health");
		assertThat(check.getStatus()).isNull();
	}

	@Test
	void initialStatusIsAppliedToHttpCheck() {
		this.properties.setHealthCheckInitialStatus(CheckStatus.PASSING);

		NewService.Check check = ConsulAutoRegistration.createCheck(8080, this.heartbeatProperties, this.properties);

		assertThat(check.getHttp()).isEqualTo("http://localhost:8080/actuator/health");
		assertThat(check.getStatus()).isEqualTo("passing");
	}

	@Test
	void initialStatusIsAppliedToTtlCheck() {
		this.heartbeatProperties.setEnabled(true);
		this.properties.setHealthCheckInitialStatus(CheckStatus.WARNING);

		NewService.Check check = ConsulAutoRegistration.createCheck(8080, this.heartbeatProperties, this.properties);

		assertThat(check.getTtl()).isEqualTo("30s");
		assertThat(check.getStatus()).isEqualTo("warning");
	}

	@Test
	void unknownInitialStatusIsRejected() {
		this.properties.setHealthCheckInitialStatus(CheckStatus.UNKNOWN);

		assertThatIllegalArgumentException()
			.isThrownBy(() -> ConsulAutoRegistration.createCheck(8080, this.heartbeatProperties, this.properties))
			.withMessageContaining("spring.cloud.consul.discovery.health-check-initial-status");
	}

}
