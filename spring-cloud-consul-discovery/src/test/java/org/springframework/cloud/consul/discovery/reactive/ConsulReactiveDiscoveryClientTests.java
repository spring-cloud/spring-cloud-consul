/*
 * Copyright 2019-present the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.ConsulClient.QueryParams;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.model.http.health.HealthService;
import org.springframework.http.ResponseEntity;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
		when(consulClient.getCatalogServices(any(String.class), any(QueryParams.class)))
			.thenThrow(new RuntimeException("Possible runtime exception"));
		StepVerifier.create(services).expectNextCount(0).expectComplete().verify();
		verify(consulClient).getCatalogServices(any(String.class), any(QueryParams.class));
	}

	@Test
	public void shouldReturnFluxOfServices() {
		Flux<String> services = client.getServices();
		when(consulClient.getCatalogServices(any(String.class), any(QueryParams.class)))
			.thenReturn(consulServicesResponse());
		StepVerifier.create(services).expectNext("my-service").expectComplete().verify();
		verify(properties).getAclToken();
		verify(consulClient).getCatalogServices(any(String.class), any(QueryParams.class));
	}

	@Test
	public void aclTokenToStringMasked() {
		InetUtils inetUtils = mock(InetUtils.class);
		when(inetUtils.findFirstNonLoopbackHostInfo()).thenReturn(mock(InetUtils.HostInfo.class));
		ConsulDiscoveryProperties consulDiscoveryProperties = new ConsulDiscoveryProperties(inetUtils);
		consulDiscoveryProperties.setAclToken("myAclToken");
		assertThat(consulDiscoveryProperties.toString()).doesNotContain("myAclToken").contains("******");
	}

	@Test
	public void shouldReturnFluxOfServicesWithAclToken() {
		when(properties.getAclToken()).thenReturn("myAclToken");
		when(consulClient.getCatalogServices(any(String.class), any(QueryParams.class)))
			.thenReturn(consulServicesResponse());
		Flux<String> services = client.getServices();
		StepVerifier.create(services).expectNext("my-service").expectComplete().verify();
		verify(properties, times(1)).getAclToken();
		verify(consulClient).getCatalogServices(any(String.class), any(QueryParams.class));
	}

	@Test
	public void shouldReturnEmptyFluxForNonExistingService() {
		configureCommonProperties();
		when(consulClient.getHealthServices(eq("nonexistent-service"))).thenReturn(emptyConsulInstancesResponse());
		Flux<ServiceInstance> instances = client.getInstances("nonexistent-service");
		StepVerifier.create(instances).expectNextCount(0).expectComplete().verify();
		verify(properties).getAclToken();
		verify(consulClient).getHealthServices(eq("nonexistent-service"));
	}

	@Test
	public void shouldReturnEmptyFluxWhenConsulFails() {
		configureCommonProperties();
		when(consulClient.getHealthServices(eq("existing-service")))
			.thenThrow(new RuntimeException("Possible runtime exception"));
		Flux<ServiceInstance> instances = client.getInstances("existing-service");
		StepVerifier.create(instances).expectNextCount(0).expectComplete().verify();
		verify(consulClient).getHealthServices(eq("existing-service"));
	}

	@Test
	public void shouldReturnFluxOfServiceInstances() {
		configureCommonProperties();
		ResponseEntity<List<HealthService>> response = consulInstancesResponse();
		when(consulClient.getHealthServices(eq("existing-service"))).thenReturn(response);
		Flux<ServiceInstance> instances = client.getInstances("existing-service");
		StepVerifier.create(instances).expectNextCount(1).expectComplete().verify();
		verify(properties).getAclToken();
		verify(properties).getQueryTagsForService("existing-service");
		verify(properties).isQueryPassing();
		verify(consulClient).getHealthServices(eq("existing-service"));
	}

	@Test
	public void shouldReturnFluxOfServiceInstancesWithAclToken() {
		configureCommonProperties();
		when(properties.getAclToken()).thenReturn("aclToken");
		ResponseEntity<List<HealthService>> response = consulInstancesResponse();
		when(consulClient.getHealthServices(eq("existing-service"))).thenReturn(response);
		Flux<ServiceInstance> instances = client.getInstances("existing-service");
		StepVerifier.create(instances).expectNextCount(1).expectComplete().verify();
		verify(properties, times(1)).getAclToken();
		verify(properties).getQueryTagsForService("existing-service");
		verify(properties).isQueryPassing();
		verify(consulClient).getHealthServices(eq("existing-service"));
	}

	private ResponseEntity<Map<String, List<String>>> consulServicesResponse() {
		return ResponseEntity.ok(singletonMap("my-service", singletonList(""))); // , 0L,
																					// true,
																					// System.currentTimeMillis());
	}

	private void configureCommonProperties() {
		when(properties.getQueryTagsForService(anyString())).thenReturn(new String[] { "queryTag" });
		when(properties.isQueryPassing()).thenReturn(false);
	}

	private ResponseEntity<List<HealthService>> emptyConsulInstancesResponse() {
		return ResponseEntity.ok(emptyList()); // , 0L, true, System.currentTimeMillis());
	}

	private ResponseEntity<List<HealthService>> consulInstancesResponse() {
		HealthService healthService = mock(HealthService.class);
		HealthService.Service service = mock(HealthService.Service.class);

		when(healthService.getService()).thenReturn(service);
		when(service.getAddress()).thenReturn("localhost");
		when(service.getPort()).thenReturn(443);
		lenient().when(service.getTags()).thenReturn(singletonList("secure=true"));

		return ResponseEntity.ok(singletonList(healthService)); // , 0L, true,
																// System.currentTimeMillis());
	}

}
