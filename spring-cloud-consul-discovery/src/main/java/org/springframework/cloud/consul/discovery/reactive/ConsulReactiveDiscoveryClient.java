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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Consul version of {@link ReactiveDiscoveryClient}.
 *
 * @author Tim Ysewyn
 * @author Chris Bono
 */
public class ConsulReactiveDiscoveryClient implements ReactiveDiscoveryClient {

	private static final Logger logger = LoggerFactory.getLogger(ConsulReactiveDiscoveryClient.class);

	private final ConsulClient client;

	private final ConsulDiscoveryProperties properties;

	public ConsulReactiveDiscoveryClient(ConsulClient client, ConsulDiscoveryProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public String description() {
		return "Spring Cloud Consul Reactive Discovery Client";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		return Flux.defer(() -> {
			List<ServiceInstance> instances = new ArrayList<>();
			for (HealthService healthService : getHealthServices(serviceId)) {
				instances.add(new ConsulServiceInstance(healthService, serviceId));
			}
			return Flux.fromIterable(instances);
		}).onErrorResume(exception -> {
			logger.error("Error getting instances from Consul.", exception);
			return Flux.empty();
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private List<HealthService> getHealthServices(String serviceId) {
		HealthServicesRequest.Builder requestBuilder = HealthServicesRequest.newBuilder()
				.setPassing(properties.isQueryPassing()).setQueryParams(QueryParams.DEFAULT)
				.setToken(properties.getAclToken());
		String[] queryTags = properties.getQueryTagsForService(serviceId);
		if (queryTags != null) {
			requestBuilder.setTags(queryTags);
		}
		HealthServicesRequest request = requestBuilder.build();

		Response<List<HealthService>> services = client.getHealthServices(serviceId, request);

		/**
		 * We need to filter out some false health nodes with empty port,
		 * otherwise NullPointerException will be reported when creating DefaultServiceInstance
		 */
		List<HealthService> effectiveServices = services == null ? Collections.emptyList() : services.getValue();
		return effectiveServices == null ? Collections.emptyList() : effectiveServices.stream()
				.filter(ser -> ser.getService() != null && ser.getService().getPort() != null)
				.collect(toList());
	}

	@Override
	public Flux<String> getServices() {
		return Flux.defer(() -> {
			CatalogServicesRequest request = CatalogServicesRequest.newBuilder().setToken(properties.getAclToken())
					.setQueryParams(QueryParams.DEFAULT).build();
			Response<Map<String, List<String>>> services = client.getCatalogServices(request);
			return services == null ? Flux.empty() : Flux.fromIterable(services.getValue().keySet());
		}).onErrorResume(exception -> {
			logger.error("Error getting services from Consul.", exception);
			return Flux.empty();
		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public int getOrder() {
		return properties.getOrder();
	}

}
