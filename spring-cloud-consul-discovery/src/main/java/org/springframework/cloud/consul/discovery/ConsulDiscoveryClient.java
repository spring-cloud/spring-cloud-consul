/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.consul.discovery.ConsulServerUtils.findHost;
import static org.springframework.cloud.consul.discovery.ConsulServerUtils.getMetadata;

/**
 * @author Spencer Gibb
 * @author Joe Athman
 * @author Tim Ysewyn
 */
public class ConsulDiscoveryClient implements DiscoveryClient {

	private static final Log log = LogFactory.getLog(ConsulDiscoveryClient.class);

	@Deprecated
	public interface LocalResolver {
		String getInstanceId();
		Integer getPort();
	}

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
		return getInstances(serviceId, QueryParams.DEFAULT);
	}

	public List<ServiceInstance> getInstances(final String serviceId,
			final QueryParams queryParams) {
		List<ServiceInstance> instances = new ArrayList<>();

		addInstancesToList(instances, serviceId, queryParams);

		return instances;
	}

	private void addInstancesToList(List<ServiceInstance> instances, String serviceId,
			QueryParams queryParams) {

		String aclToken = properties.getAclToken();
		Response<List<HealthService>> services;
		if (StringUtils.hasText(aclToken)) {
			services = client.getHealthServices(serviceId,
					this.properties.getDefaultQueryTag(),
					this.properties.isQueryPassing(), queryParams, aclToken);
		}
		else {
			services = client.getHealthServices(serviceId,
					this.properties.getDefaultQueryTag(),
					this.properties.isQueryPassing(), queryParams);
		}
		for (HealthService service : services.getValue()) {
			String host = findHost(service);

			Map<String, String> metadata = getMetadata(service);
			boolean secure = false;
			if (metadata.containsKey("secure")) {
				secure = Boolean.parseBoolean(metadata.get("secure"));
			}
			instances.add(new DefaultServiceInstance(service.getService().getId(), serviceId, host, service
					.getService().getPort(), secure, metadata));
		}
	}

	public List<ServiceInstance> getAllInstances() {
		List<ServiceInstance> instances = new ArrayList<>();

		Response<Map<String, List<String>>> services = client
				.getCatalogServices(QueryParams.DEFAULT);
		for (String serviceId : services.getValue().keySet()) {
			addInstancesToList(instances, serviceId, QueryParams.DEFAULT);
		}
		return instances;
	}

	@Override
	public List<String> getServices() {
		String aclToken = properties.getAclToken();

		if (StringUtils.hasText(aclToken)) {
			return new ArrayList<>(client.getCatalogServices(QueryParams.DEFAULT, aclToken).getValue()
					.keySet());
		} else {
			return new ArrayList<>(client.getCatalogServices(QueryParams.DEFAULT).getValue()
					.keySet());
		}
	}

	@Override
	public int getOrder() {
		return this.properties.getOrder();
	}
}
