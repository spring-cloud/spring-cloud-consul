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

import java.util.Optional;

import com.ecwid.consul.v1.health.model.Check.CheckStatus;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;

import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.OUT_OF_SERVICE;
import static org.springframework.boot.actuate.health.Status.UP;

/**
 * Leverages Spring Boot Actuator health endpoint to determine the current health of the
 * application.
 *
 * @author Chris Bono
 */
public class ActuatorHealthApplicationStatusProvider implements ApplicationStatusProvider {

	private HealthEndpoint healthEndpoint;

	private HeartbeatProperties heartbeatProperties;

	public ActuatorHealthApplicationStatusProvider(HealthEndpoint healthEndpoint,
			HeartbeatProperties heartbeatProperties) {
		this.healthEndpoint = healthEndpoint;
		this.heartbeatProperties = heartbeatProperties;
	}

	/**
	 * Gets the current actuator health status and converts to Consul check status using
	 * the following mapping:
	 * <ul>
	 * <li>{@link Status#UP} -> {@link CheckStatus#PASSING}</li>
	 * <li>{@link Status#DOWN} or {@link Status#OUT_OF_SERVICE} ->
	 * {@link CheckStatus#CRITICAL}</li>
	 * <li>Otherwise {@link CheckStatus#UNKNOWN}</li>
	 * </ul>
	 * .
	 * @return the check status based on the actuator health status (see above for
	 * mapping)
	 */
	@Override
	public CheckStatus currentStatus() {
		String healthGroup = heartbeatProperties.getActuatorHealthGroup();
		String[] path = healthGroup == null ? new String[0] : new String[] { healthGroup };
		return Optional.ofNullable(healthEndpoint.healthForPath(path)).map(HealthComponent::getStatus)
				.map(this::healthStatusToCheckStatus).orElse(CheckStatus.UNKNOWN);
	}

	private CheckStatus healthStatusToCheckStatus(Status healthStatus) {
		if (healthStatus == UP) {
			return CheckStatus.PASSING;
		}
		if (healthStatus == DOWN || healthStatus == OUT_OF_SERVICE) {
			return CheckStatus.CRITICAL;
		}
		return CheckStatus.UNKNOWN;
	}

}
