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

package org.springframework.cloud.consul;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link ConsulHealthIndicator} when it's in the DOWN status.
 *
 * @author Lomesh Patel (lomeshpatel)
 */
@SpringBootTest
public class ConsulHealthIndicatorDownTest {

	@MockitoBean
	private ConsulClient consulClient;

	@Autowired
	private HealthEndpoint healthEndpoint;

	@Test
	public void statusIsDownWhenConsulClientFailsToGetLeaderStatus() {
		when(consulClient.getStatusLeader()).thenThrow(new RuntimeException("no leader"));
		assertThat(this.healthEndpoint.health().getStatus()).as("health status was not DOWN").isEqualTo(Status.DOWN);
		verify(consulClient).getStatusLeader();
	}

	@Test
	public void statusIsDownWhenConsulClientFailsToGetServices() {
		String leaderStatus = "OK";
		when(consulClient.getStatusLeader()).thenReturn(ResponseEntity.ok(leaderStatus));
		when(consulClient.getCatalogServices(null, null)).thenThrow(new RuntimeException("no services"));
		assertThat(this.healthEndpoint.health().getStatus()).as("health status was not DOWN").isEqualTo(Status.DOWN);
		verify(consulClient).getCatalogServices(null, null);
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	protected static class TestConfig {

	}

}
