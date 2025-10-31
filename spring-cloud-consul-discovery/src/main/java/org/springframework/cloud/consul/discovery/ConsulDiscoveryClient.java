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

package org.springframework.cloud.consul.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.ConsulClient.QueryParams;
import org.springframework.cloud.consul.model.http.health.HealthService;
import org.springframework.http.ResponseEntity;

/**
 * @author Spencer Gibb
 * @author Joe Athman
 * @author Tim Ysewyn
 * @author Chris Bono
 */
public class ConsulDiscoveryClient implements DiscoveryClient {

	private final ConsulClient client;

	private final ConsulDiscoveryProperties properties;

	public ConsulDiscoveryClient(ConsulClient client, ConsulDiscoveryProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public String description() {
		return "Spring Cloud Consul Discovery Client";
	}

	@Override
	public List<ServiceInstance> getInstances(final String serviceId) {
		ConsulDiscoveryProperties.ConsistencyMode consistencyModeProp = this.properties.getConsistencyMode();
		ConsulClient.ConsistencyMode consistencyMode = ConsulClient.ConsistencyMode.DEFAULT;
		if (consistencyModeProp != null) {
			consistencyMode = ConsulClient.ConsistencyMode.valueOf(consistencyModeProp.name().toUpperCase(Locale.ROOT));
		}
		return getInstances(serviceId, new QueryParams(consistencyMode));
	}

	public List<ServiceInstance> getInstances(final String serviceId, final QueryParams queryParams) {
		List<ServiceInstance> instances = new ArrayList<>();

		addInstancesToList(instances, serviceId, queryParams);

		return instances;
	}

	private void addInstancesToList(List<ServiceInstance> instances, String serviceId, QueryParams queryParams) {
		String[] queryTags = properties.getQueryTagsForService(serviceId);
		List<String> tags = null;
		if (queryTags != null) {
			tags = Arrays.asList(queryTags);
		}

		ResponseEntity<List<HealthService>> healthServices = client.getHealthServices(serviceId,
				properties.isQueryPassing(), properties.getAclToken(), tags, queryParams);

		for (HealthService service : healthServices.getBody()) {
			instances.add(new ConsulServiceInstance(service, serviceId));
		}
	}

	public List<ServiceInstance> getAllInstances() {
		List<ServiceInstance> instances = new ArrayList<>();
		Map<String, List<String>> catalogServices = client
			.getCatalogServices(properties.getAclToken(), QueryParams.DEFAULT)
			.getBody();

		for (String serviceId : catalogServices.keySet()) {
			addInstancesToList(instances, serviceId, null);
		}
		return instances;
	}

	@Override
	public List<String> getServices() {
		Map<String, List<String>> catalogServices = client
			.getCatalogServices(properties.getAclToken(), QueryParams.DEFAULT)
			.getBody();
		return new ArrayList<>(catalogServices.keySet());
	}

	@Override
	public void probe() {
		this.client.getStatusLeader();
	}

	@Override
	public int getOrder() {
		return this.properties.getOrder();
	}

}
