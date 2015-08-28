/*
 * Copyright 2013-2015 the original author or authors.
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

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.model.CatalogService;

/**
 * @author Spencer Gibb
 */
public class ConsulDiscoveryClient implements DiscoveryClient {

	private ConsulLifecycle lifecycle;

	private ConsulClient client;

	private ConsulDiscoveryProperties properties;

	public ConsulDiscoveryClient(ConsulClient client, ConsulLifecycle lifecycle,
								 ConsulDiscoveryProperties properties) {
		this.client = client;
		this.lifecycle = lifecycle;
		this.properties = properties;
	}

	@Override
	public String description() {
		return "Spring Cloud Consul Discovery Client";
	}

	@Override
	public ServiceInstance getLocalServiceInstance() {
		Response<Map<String, Service>> agentServices = client.getAgentServices();
		Service service = agentServices.getValue().get(lifecycle.getServiceId());
		if (service == null) {
			throw new IllegalStateException("Unable to locate service in consul agent: "
					+ lifecycle.getServiceId());
		}
		String host = "localhost";
		Response<Self> agentSelf = client.getAgentSelf();
		Member member = agentSelf.getValue().getMember();
		if (member != null) {
			if (properties.isPreferIpAddress()) {
				host = member.getAddress();
			} else if (StringUtils.hasText(member.getName())) {
				host = member.getName();
			}
		}
		return new DefaultServiceInstance(service.getId(), host, service.getPort(), false);
	}

	@Override
	public List<ServiceInstance> getInstances(final String serviceId) {
		List<ServiceInstance> instances = new ArrayList<>();

		addInstancesToList(instances, serviceId);

		return instances;
	}

	private void addInstancesToList(List<ServiceInstance> instances, String serviceId) {
		Response<List<CatalogService>> services = client.getCatalogService(serviceId,
				QueryParams.DEFAULT);
		for (CatalogService service : services.getValue()) {
			String host;
			if (this.properties.isPreferIpAddress()) {
				host = service.getAddress();
			} else {
				host = service.getNode();
			}
			instances.add(new DefaultServiceInstance(serviceId, host,
					service.getServicePort(), false));
		}
	}

	public List<ServiceInstance> getAllInstances() {
		List<ServiceInstance> instances = new ArrayList<>();

		Response<Map<String, List<String>>> services = client
				.getCatalogServices(QueryParams.DEFAULT);
		for (String serviceId : services.getValue().keySet()) {
			addInstancesToList(instances, serviceId);
		}
		return instances;
	}

	@Override
	public List<String> getServices() {
		return new ArrayList<>(client.getCatalogServices(QueryParams.DEFAULT).getValue()
				.keySet());
	}
}
