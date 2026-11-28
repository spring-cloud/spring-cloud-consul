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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.model.http.health.HealthService;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

/**
 * A {@link ServiceInstanceListSupplier} implementation that only provides the
 * {@link ConsulServiceInstance Consul service instances} whose Consul health checks are
 * all {@link Check.CheckStatus#PASSING passing}. Instances with a check that is
 * {@code warning}, {@code critical} or in any other state are left out, so the load
 * balancer does not pick them.
 * <p>
 * This allows the health of the instances to be taken into account for load balancing
 * only, while the {@link org.springframework.cloud.client.discovery.DiscoveryClient}
 * still returns all instances (with
 * {@code spring.cloud.consul.discovery.query-passing=false}).
 * <p>
 * Instances that are not Consul service instances, or that do not carry the Consul health
 * information, are returned as they are, since their health is unknown.
 *
 * @author Asish Kumar
 */
public class PassingChecksServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {

	private static final Log log = LogFactory.getLog(PassingChecksServiceInstanceListSupplier.class);

	public PassingChecksServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
		super(delegate);
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return getDelegate().get().map(this::filteredByPassingChecks);
	}

	@Override
	public Flux<List<ServiceInstance>> get(Request request) {
		return getDelegate().get(request).map(this::filteredByPassingChecks);
	}

	private List<ServiceInstance> filteredByPassingChecks(List<ServiceInstance> serviceInstances) {
		List<ServiceInstance> filteredInstances = new ArrayList<>(serviceInstances.size());
		for (ServiceInstance serviceInstance : serviceInstances) {
			if (isPassingChecks(serviceInstance)) {
				filteredInstances.add(serviceInstance);
			}
		}
		return filteredInstances;
	}

	private boolean isPassingChecks(ServiceInstance serviceInstance) {
		HealthService healthService = null;
		if (serviceInstance instanceof ConsulServiceInstance consulServiceInstance) {
			healthService = consulServiceInstance.getHealthService();
		}
		if (healthService == null) {
			if (log.isDebugEnabled()) {
				log.debug("Unable to determine the health of service instance of type " + serviceInstance.getClass()
						+ ", " + serviceInstance);
			}
			return true;
		}
		List<Check> checks = healthService.getChecks();
		if (checks != null) {
			for (Check check : checks) {
				if (check.getStatus() != Check.CheckStatus.PASSING) {
					return false;
				}
			}
		}
		return true;
	}

}
