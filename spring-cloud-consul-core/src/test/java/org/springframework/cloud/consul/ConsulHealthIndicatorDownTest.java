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

package org.springframework.cloud.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link ConsulHealthIndicator} when its in the DOWN status.
 *
 * @author Lomesh Patel (lomeshpatel)
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsulHealthIndicatorDownTest {

	@MockBean
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
		Response<String> leaderStatus = new Response<>("OK", 5150L, true, System.currentTimeMillis());
		when(consulClient.getStatusLeader()).thenReturn(leaderStatus);
		when(consulClient.getCatalogServices(any(CatalogServicesRequest.class)))
				.thenThrow(new RuntimeException("no services"));
		assertThat(this.healthEndpoint.health().getStatus()).as("health status was not DOWN").isEqualTo(Status.DOWN);
		verify(consulClient).getCatalogServices(any(CatalogServicesRequest.class));
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	protected static class TestConfig {

	}

}
