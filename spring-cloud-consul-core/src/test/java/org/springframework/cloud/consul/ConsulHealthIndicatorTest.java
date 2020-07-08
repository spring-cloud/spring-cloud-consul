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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConsulHealthIndicator}.
 *
 * @author Chris Bono
 */
@RunWith(SpringRunner.class)
public class ConsulHealthIndicatorTest {

	private ConsulClient consulClient;

	private ConsulHealthIndicator indicator;

	private ConsulHealthIndicatorProperties indicatorProps;

	@Before
	public void setUp() {
		consulClient = mock(ConsulClient.class);

		Response<String> leaderStatus = newResponse("OK");
		when(consulClient.getStatusLeader()).thenReturn(leaderStatus);

		Map<String, List<String>> services = Collections.singletonMap("aus",
				Arrays.asList("service1", "service2"));
		when(consulClient.getCatalogServices(any(CatalogServicesRequest.class)))
				.thenReturn(newResponse(services));

		indicatorProps = new ConsulHealthIndicatorProperties();
		indicator = new ConsulHealthIndicator(consulClient, indicatorProps);
	}

	@Test
	public void indicatorUpWithServiceQuery() {
		Health health = indicator.getHealth(true);
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsKeys("leader", "services");
		verify(consulClient).getStatusLeader();
		verify(consulClient).getCatalogServices(any(CatalogServicesRequest.class));
	}

	@Test
	public void indicatorUpWithoutServiceQuery() {
		indicatorProps.setIncludeServicesQuery(false);
		Health health = indicator.getHealth(true);
		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsKeys("leader");
		verify(consulClient).getStatusLeader();
		verify(consulClient, never())
				.getCatalogServices(any(CatalogServicesRequest.class));
	}

	@Test
	public void indicatorDownWhenConsulFailsToCheckLeaderStatus() {
		when(consulClient.getStatusLeader()).thenThrow(new RuntimeException("no leader"));
		Health health = indicator.getHealth(true);
		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsKeys("error");
		verify(consulClient).getStatusLeader();
		verify(consulClient, never())
				.getCatalogServices(any(CatalogServicesRequest.class));
	}

	@Test
	public void indicatorDownWhenConsulFailsToRetrieveServices() {
		when(consulClient.getCatalogServices(any(CatalogServicesRequest.class)))
				.thenThrow(new RuntimeException("no services"));
		Health health = indicator.getHealth(true);
		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsKeys("error");
		verify(consulClient).getStatusLeader();
		verify(consulClient).getCatalogServices(any(CatalogServicesRequest.class));
	}

	private <T> Response<T> newResponse(T data) {
		return new Response<>(data, 5150L, true, System.currentTimeMillis());
	}

}
