/*
 * Copyright 2019-2019 the original author or authors.
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

package org.springframework.cloud.consul.discovery.reactive;

import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tim Ysewyn
 */
@ExtendWith(MockitoExtension.class)
class ConsulReactiveDiscoveryClientTests {

	@Mock
	private ConsulClient consulClient;

	@Mock
	private ConsulDiscoveryProperties properties;

	@InjectMocks
	private ConsulReactiveDiscoveryClient client;

	@Test
	public void verifyDefaults() {
		when(properties.getOrder()).thenReturn(1);
		assertThat(client.description()).isEqualTo("Spring Cloud Consul Reactive Discovery Client");
		assertThat(client.getOrder()).isEqualTo(1);
	}

	@Test
	public void shouldReturnEmptyFluxOfServicesWhenConsulFails() {
		Flux<String> services = client.getServices();
		when(consulClient.getCatalogServices(any(CatalogServicesRequest.class)))
				.thenThrow(new RuntimeException("Possible runtime exception"));
		StepVerifier.create(services).expectNextCount(0).expectComplete().verify();
		verify(consulClient).getCatalogServices(any(CatalogServicesRequest.class));
	}

	@Test
	public void shouldReturnFluxOfServices() {
		Flux<String> services = client.getServices();
		when(consulClient.getCatalogServices(any(CatalogServicesRequest.class))).thenReturn(consulServicesResponse());
		StepVerifier.create(services).expectNext("my-service").expectComplete().verify();
		verify(properties).getAclToken();
		verify(consulClient).getCatalogServices(any(CatalogServicesRequest.class));
	}

	@Test
	public void shouldReturnFluxOfServicesWithAclToken() {
		when(properties.getAclToken()).thenReturn("aclToken");
		when(consulClient.getCatalogServices(any(CatalogServicesRequest.class))).thenReturn(consulServicesResponse());
		Flux<String> services = client.getServices();
		StepVerifier.create(services).expectNext("my-service").expectComplete().verify();
		verify(properties, times(1)).getAclToken();
		verify(consulClient).getCatalogServices(any(CatalogServicesRequest.class));
	}

	@Test
	public void shouldReturnEmptyFluxForNonExistingService() {
		configureCommonProperties();
		when(consulClient.getHealthServices(eq("nonexistent-service"), any(HealthServicesRequest.class)))
				.thenReturn(emptyConsulInstancesResponse());
		Flux<ServiceInstance> instances = client.getInstances("nonexistent-service");
		StepVerifier.create(instances).expectNextCount(0).expectComplete().verify();
		verify(properties).getAclToken();
		verify(consulClient).getHealthServices(eq("nonexistent-service"), any());
	}

	@Test
	public void shouldReturnEmptyFluxWhenConsulFails() {
		configureCommonProperties();
		when(consulClient.getHealthServices(eq("existing-service"), any(HealthServicesRequest.class)))
				.thenThrow(new RuntimeException("Possible runtime exception"));
		Flux<ServiceInstance> instances = client.getInstances("existing-service");
		StepVerifier.create(instances).expectNextCount(0).expectComplete().verify();
		verify(consulClient).getHealthServices(eq("existing-service"), any());
	}

	@Test
	public void shouldReturnFluxOfServiceInstances() {
		configureCommonProperties();
		Response<List<HealthService>> response = consulInstancesResponse();
		when(consulClient.getHealthServices(eq("existing-service"), any(HealthServicesRequest.class)))
				.thenReturn(response);
		Flux<ServiceInstance> instances = client.getInstances("existing-service");
		StepVerifier.create(instances).expectNextCount(1).expectComplete().verify();
		verify(properties).getAclToken();
		verify(properties).getDefaultQueryTag();
		verify(properties).isQueryPassing();
		verify(consulClient).getHealthServices(eq("existing-service"), any());
	}

	@Test
	public void shouldReturnFluxOfServiceInstancesWithAclToken() {
		configureCommonProperties();
		when(properties.getAclToken()).thenReturn("aclToken");
		Response<List<HealthService>> response = consulInstancesResponse();
		when(consulClient.getHealthServices(eq("existing-service"), any(HealthServicesRequest.class)))
				.thenReturn(response);
		Flux<ServiceInstance> instances = client.getInstances("existing-service");
		StepVerifier.create(instances).expectNextCount(1).expectComplete().verify();
		verify(properties, times(1)).getAclToken();
		verify(properties).getDefaultQueryTag();
		verify(properties).isQueryPassing();
		verify(consulClient).getHealthServices(eq("existing-service"), any());
	}

	private Response<Map<String, List<String>>> consulServicesResponse() {
		return new Response<>(singletonMap("my-service", singletonList("")), 0L, true, System.currentTimeMillis());
	}

	private void configureCommonProperties() {
		when(properties.getDefaultQueryTag()).thenReturn("queryTag");
		when(properties.isQueryPassing()).thenReturn(false);
	}

	private Response<List<HealthService>> emptyConsulInstancesResponse() {
		return new Response<>(emptyList(), 0L, true, System.currentTimeMillis());
	}

	private Response<List<HealthService>> consulInstancesResponse() {
		HealthService healthService = mock(HealthService.class);
		HealthService.Service service = mock(HealthService.Service.class);

		when(healthService.getService()).thenReturn(service);
		when(service.getAddress()).thenReturn("localhost");
		when(service.getPort()).thenReturn(443);
		lenient().when(service.getTags()).thenReturn(singletonList("secure=true"));

		return new Response<>(singletonList(healthService), 0L, true, System.currentTimeMillis());
	}

}
