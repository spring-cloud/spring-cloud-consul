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

package org.springframework.cloud.consul.discovery.loadbalancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.model.http.health.HealthService;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PassingChecksServiceInstanceListSupplier}.
 *
 * @author Asish Kumar
 */
class PassingChecksServiceInstanceListSupplierTests {

	private final ServiceInstanceListSupplier delegate = mock(ServiceInstanceListSupplier.class);

	private final PassingChecksServiceInstanceListSupplier supplier = new PassingChecksServiceInstanceListSupplier(
			delegate);

	private final ServiceInstance passing = consulServiceInstance("passing", Check.CheckStatus.PASSING,
			Check.CheckStatus.PASSING);

	private final ServiceInstance warning = consulServiceInstance("warning", Check.CheckStatus.PASSING,
			Check.CheckStatus.WARNING);

	private final ServiceInstance critical = consulServiceInstance("critical", Check.CheckStatus.CRITICAL,
			Check.CheckStatus.PASSING);

	private final ServiceInstance unknown = consulServiceInstance("unknown", Check.CheckStatus.PASSING,
			Check.CheckStatus.UNKNOWN);

	@Test
	void shouldOnlyReturnInstancesWithAllChecksPassing() {
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(passing, warning, critical, unknown)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).containsExactly(passing);
	}

	@Test
	void shouldFilterInstancesReturnedForRequest() {
		Request<DefaultRequestContext> request = new DefaultRequest<>(new DefaultRequestContext());
		ServiceInstance otherPassing = consulServiceInstance("other-passing", Check.CheckStatus.PASSING);
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(passing, warning)));
		when(delegate.get(request)).thenReturn(Flux.just(Arrays.asList(otherPassing, critical)));

		List<ServiceInstance> filtered = supplier.get(request).blockFirst();

		assertThat(filtered).containsExactly(otherPassing);
	}

	@Test
	void shouldReturnEmptyListWhenNoInstancePassesItsChecks() {
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(warning, critical, unknown)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).isEmpty();
	}

	@Test
	void shouldReturnInstancesWithoutChecks() {
		ServiceInstance noChecks = consulServiceInstance("no-checks");
		HealthService nullChecksHealthService = healthService("null-checks");
		nullChecksHealthService.setChecks(null);
		ServiceInstance nullChecks = new ConsulServiceInstance(nullChecksHealthService, "test");
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(noChecks, nullChecks)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).containsExactly(noChecks, nullChecks);
	}

	@Test
	void shouldReturnInstancesWithUnknownHealth() {
		ServiceInstance withoutHealthService = new ConsulServiceInstance("without-health-service", "test", "localhost",
				8080, false);
		ServiceInstance notConsul = new DefaultServiceInstance("not-consul", "test", "localhost", 8080, false);
		when(delegate.get()).thenReturn(Flux.just(Arrays.asList(withoutHealthService, notConsul, critical)));

		List<ServiceInstance> filtered = supplier.get().blockFirst();

		assertThat(filtered).containsExactly(withoutHealthService, notConsul);
	}

	@Test
	void shouldFilterEachListPublishedByDelegate() {
		when(delegate.get())
			.thenReturn(Flux.just(Arrays.asList(passing, critical), Arrays.asList(warning), Collections.emptyList()));

		List<List<ServiceInstance>> filtered = supplier.get().collectList().block();

		assertThat(filtered).containsExactly(Collections.singletonList(passing), Collections.emptyList(),
				Collections.emptyList());
	}

	@Test
	void shouldReturnServiceIdOfDelegate() {
		when(delegate.getServiceId()).thenReturn("test");

		assertThat(supplier.getServiceId()).isEqualTo("test");
	}

	private static ServiceInstance consulServiceInstance(String instanceId, Check.CheckStatus... statuses) {
		HealthService healthService = healthService(instanceId);
		List<Check> checks = new ArrayList<>();
		for (int i = 0; i < statuses.length; i++) {
			Check check = new Check();
			check.setCheckId(instanceId + "-check-" + i);
			check.setStatus(statuses[i]);
			checks.add(check);
		}
		healthService.setChecks(checks);
		return new ConsulServiceInstance(healthService, "test");
	}

	private static HealthService healthService(String instanceId) {
		HealthService.Service service = new HealthService.Service();
		service.setId(instanceId);
		service.setService("test");
		service.setAddress("localhost");
		service.setPort(8080);
		HealthService healthService = new HealthService();
		healthService.setService(service);
		healthService.setNode(new HealthService.Node());
		return healthService;
	}

}
