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
import java.util.function.Function;
import java.util.function.Supplier;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.consul.discovery.ConsulServerUtils.findHost;
import static org.springframework.cloud.consul.discovery.ConsulServerUtils.getMetadata;

/**
 * Consul version of {@link ReactiveDiscoveryClient}.
 *
 * @author Tim Ysewyn
 */
public class ConsulReactiveDiscoveryClient implements ReactiveDiscoveryClient {

	private static final Logger logger = LoggerFactory
			.getLogger(ConsulReactiveDiscoveryClient.class);

	private final ConsulClient client;

	private final ConsulDiscoveryProperties properties;

	public ConsulReactiveDiscoveryClient(ConsulClient client,
			ConsulDiscoveryProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public String description() {
		return "Spring Cloud Consul Reactive Discovery Client";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		return Flux.defer(getHealthServices(serviceId))
				.map(mapToServiceInstance(serviceId)).onErrorResume(exception -> {
					logger.error("Error getting instances from Consul.", exception);
					return Flux.empty();
				}).subscribeOn(Schedulers.elastic());
	}

	private Supplier<? extends Publisher<HealthService>> getHealthServices(
			String serviceId) {
		return () -> {
			Response<List<HealthService>> services = StringUtils
					.hasText(properties.getAclToken())
							? this.client.getHealthServices(serviceId,
									this.properties.getDefaultQueryTag(),
									this.properties.isQueryPassing(), QueryParams.DEFAULT,
									properties.getAclToken())
							: this.client.getHealthServices(serviceId,
									this.properties.getDefaultQueryTag(),
									this.properties.isQueryPassing(),
									QueryParams.DEFAULT);
			return services == null ? Flux.empty()
					: Flux.fromIterable(services.getValue());
		};
	}

	private Function<HealthService, ServiceInstance> mapToServiceInstance(
			String serviceId) {
		return service -> {
			String host = findHost(service);
			Map<String, String> metadata = getMetadata(service);
			boolean secure = false;
			if (metadata.containsKey("secure")) {
				secure = Boolean.parseBoolean(metadata.get("secure"));
			}
			return new DefaultServiceInstance(service.getService().getId(), serviceId,
					host, service.getService().getPort(), secure, metadata);
		};
	}

	@Override
	public Flux<String> getServices() {
		return Flux.defer(() -> {
			Response<Map<String, List<String>>> services = StringUtils
					.hasText(properties.getAclToken())
							? client.getCatalogServices(QueryParams.DEFAULT,
									properties.getAclToken())
							: client.getCatalogServices(QueryParams.DEFAULT);
			return services == null ? Flux.empty()
					: Flux.fromIterable(services.getValue().keySet());
		}).onErrorResume(exception -> {
			logger.error("Error getting services from Consul.", exception);
			return Flux.empty();
		}).subscribeOn(Schedulers.elastic());
	}

	@Override
	public int getOrder() {
		return properties.getOrder();
	}

}
