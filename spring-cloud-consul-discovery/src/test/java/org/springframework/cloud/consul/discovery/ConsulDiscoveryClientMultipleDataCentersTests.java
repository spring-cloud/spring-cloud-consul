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
import java.util.List;
import java.util.Objects;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Vojislav Cuk
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		properties = { "spring.application.name=testConsulDiscovery",
				"spring.cloud.consul.discovery.prefer-ip-address=true",
				"spring.cloud.consul.discovery.metadata[foo]=bar",
				"spring.cloud.consul.discovery.failoverDataCenters[0]=dc2" },
		webEnvironment = RANDOM_PORT)
public class ConsulDiscoveryClientMultipleDataCentersTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@MockBean
	private ConsulClient consulClient;

	@Test
	public void getInstancesShouldReturnEmptyListWhenThereAreNoInstancesInLocalNorFailoverDataCenters() {

		Mockito.when(consulClient.getHealthServices(eq("testService"), any())).thenReturn(
				new Response<>(new ArrayList<>(), 0L, true, System.currentTimeMillis()));

		assertThat(discoveryClient.getInstances("testService")).isEmpty();
	}

	@Test
	public void getInstancesShouldReturnInstancesFromFailoverDataCentersWhenNoInstanceInLocalDataCenterIsAvailable() {

		Mockito.when(consulClient.getHealthServices(eq("testService"),
				argThat(r -> !StringUtils.hasLength(r.getDatacenter()))))
				.thenReturn(new Response<>(new ArrayList<>(), 0L, true,
						System.currentTimeMillis()));

		Response<List<HealthService>> response = consulInstancesResponse();
		Mockito.when(consulClient.getHealthServices(eq("testService"),
				argThat(r -> Objects.equals(r.getQueryParams().getDatacenter(), "dc2"))))
				.thenReturn(response);

		assertThat(discoveryClient.getInstances("testService")).isNotEmpty();
	}

	private Response<List<HealthService>> consulInstancesResponse() {

		HealthService healthService = mock(HealthService.class);
		HealthService.Service service = mock(HealthService.Service.class);

		when(healthService.getService()).thenReturn(service);
		when(service.getAddress()).thenReturn("localhost");
		when(service.getPort()).thenReturn(443);
		lenient().when(service.getTags()).thenReturn(singletonList("secure=true"));

		return new Response<>(singletonList(healthService), 0L, true,
				System.currentTimeMillis());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class MyTestConfig {

	}

}
