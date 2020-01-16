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

package org.springframework.cloud.consul.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import org.junit.Test;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Christian Lorenz
 */
public class ConsulServerTest {

	@Test
	public void testIsPassingChecks() {
		Set<Check.CheckStatus> acceptedStatus = new HashSet<>(
				Arrays.asList(PASSING, WARNING));
		assertThat(newServer(PASSING, acceptedStatus).isPassingChecks()).isTrue();
		assertThat(newServer(WARNING, acceptedStatus).isPassingChecks()).isTrue();
		assertThat(newServer(CRITICAL, acceptedStatus).isPassingChecks()).isFalse();
	}

	static ConsulServer newServer(Check.CheckStatus checkStatus,
			Set<Check.CheckStatus> acceptedStatus) {
		HealthService healthService = new HealthService();
		HealthService.Node node = new HealthService.Node();
		node.setAddress("nodeaddr" + checkStatus.name());
		node.setNode("nodenode" + checkStatus.name());
		healthService.setNode(node);
		HealthService.Service service = new HealthService.Service();
		service.setAddress("serviceaddr" + checkStatus.name());
		service.setId("serviceid" + checkStatus.name());
		service.setPort(8080);
		service.setService("serviceservice" + checkStatus.name());
		healthService.setService(service);
		ArrayList<Check> checks = new ArrayList<>();
		Check check = new Check();
		check.setStatus(checkStatus);
		checks.add(check);
		healthService.setChecks(checks);
		return new ConsulServer(healthService, acceptedStatus);
	}

}
