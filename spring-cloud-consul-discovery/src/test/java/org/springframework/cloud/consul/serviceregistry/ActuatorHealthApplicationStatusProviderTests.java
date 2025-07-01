/*
 * Copyright 2013-2025 the original author or authors.
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

import java.util.stream.Stream;

import com.ecwid.consul.v1.health.model.Check;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.IndicatedHealthDescriptor;
import org.springframework.boot.health.contributor.Status;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.health.contributor.Status.DOWN;
import static org.springframework.boot.health.contributor.Status.OUT_OF_SERVICE;
import static org.springframework.boot.health.contributor.Status.UNKNOWN;
import static org.springframework.boot.health.contributor.Status.UP;

/**
 * Unit tests for {@link ActuatorHealthApplicationStatusProvider}.
 *
 * @author Chris Bono
 */
@ExtendWith(MockitoExtension.class)
class ActuatorHealthApplicationStatusProviderTests {

	@Mock
	private HealthEndpoint healthEndpoint;

	@Mock
	private HeartbeatProperties heartbeatProperties;

	@InjectMocks
	private ActuatorHealthApplicationStatusProvider applicationStatusProvider;

	static Stream<Arguments> currentCheckStatusBasedOnHealthStatusArgs() {
		return Stream.of(arguments(UP, Check.CheckStatus.PASSING), arguments(DOWN, Check.CheckStatus.CRITICAL),
				arguments(OUT_OF_SERVICE, Check.CheckStatus.CRITICAL), arguments(UNKNOWN, Check.CheckStatus.UNKNOWN));
	}

	@DisplayName("currentStatus() tests")
	@ParameterizedTest(name = "{index} ==> health status ''{0}'' should map to check status ''{1}''")
	@MethodSource("currentCheckStatusBasedOnHealthStatusArgs")
	void currentCheckStatusBasedOnHealthStatus(Status healthStatus, Check.CheckStatus expectedCheckStatus) {
		IndicatedHealthDescriptor descriptor = mock(IndicatedHealthDescriptor.class);
		when(descriptor.getStatus()).thenReturn(healthStatus);
		when(healthEndpoint.healthForPath(new String[0])).thenReturn(descriptor);
		assertThat(applicationStatusProvider.currentStatus()).isEqualTo(expectedCheckStatus);
		verify(healthEndpoint).healthForPath(new String[0]);
	}

	@Test
	void currentStatusUsesHealthGroupIfSpecified() {
		when(heartbeatProperties.getActuatorHealthGroup()).thenReturn("5150");
		IndicatedHealthDescriptor descriptor = mock(IndicatedHealthDescriptor.class);
		when(descriptor.getStatus()).thenReturn(Status.OUT_OF_SERVICE);
		when(healthEndpoint.healthForPath(new String[] { "5150" })).thenReturn(descriptor);
		assertThat(applicationStatusProvider.currentStatus()).isEqualTo(Check.CheckStatus.CRITICAL);
		verify(healthEndpoint).healthForPath(new String[] { "5150" });
	}

	@Test
	void currentStatusHandlesNullHealthStatusGracefully() {
		when(healthEndpoint.healthForPath(new String[0])).thenReturn(null);
		assertThat(applicationStatusProvider.currentStatus()).isEqualTo(Check.CheckStatus.UNKNOWN);
		verify(healthEndpoint).healthForPath(new String[0]);
	}

}
