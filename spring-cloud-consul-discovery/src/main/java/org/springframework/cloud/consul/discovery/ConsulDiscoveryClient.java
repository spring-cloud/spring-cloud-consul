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

import static org.springframework.cloud.consul.discovery.ConsulServerUtils.findHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.discovery.filters.ConsulServiceDiscoveryFilter;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.health.model.HealthService;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ConsulDiscoveryClient implements DiscoveryClient {

	private final ConsulLifecycle lifecycle;

	private final ConsulClient client;

	private final ConsulDiscoveryProperties properties;

	private final ConsulServiceDiscoveryFilter filter;

	private ServerProperties serverProperties;

	public ConsulDiscoveryClient(final ConsulClient client, final ConsulLifecycle lifecycle,
			final ConsulDiscoveryProperties properties,
			final ConsulServiceDiscoveryFilter filter) {
		this.client = client;
		this.lifecycle = lifecycle;
		this.properties = properties;
		this.filter = filter;
	}

	public void setServerProperties(final ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public String description() {
		return "Spring Cloud Consul Discovery Client";
	}

	@Override
	public ServiceInstance getLocalServiceInstance() {
		Response<Map<String, Service>> agentServices = client.getAgentServices();
		Service service = agentServices.getValue().get(lifecycle.getServiceId());
		String serviceId;
		Integer port;
		List<String> tags;
		if (service == null) {
			// possibly called before registration
			log.warn("Unable to locate service in consul agent: "
					+ lifecycle.getServiceId());

			serviceId = lifecycle.getServiceId();
			port = lifecycle.getConfiguredPort();
			if (port == 0 && serverProperties != null
					&& serverProperties.getPort() != null) {
				port = serverProperties.getPort();
			}
			tags = Collections.emptyList();
		}
		else {
			serviceId = service.getId();
			port = service.getPort();
			tags = service.getTags();
		}
		String host = "localhost";
		Response<Self> agentSelf = client.getAgentSelf();
		Member member = agentSelf.getValue().getMember();
		if (member != null) {
			if (properties.isPreferIpAddress()) {
				host = member.getAddress();
			}
			else if (StringUtils.hasText(member.getName())) {
				host = member.getName();
			}
		}
		return new ConsulServiceInstance(serviceId, host, port, false, tags);
	}

	@Override
	public List<ServiceInstance> getInstances(final String serviceId) {
		List<ServiceInstance> instances = new ArrayList<>();

		addInstancesToList(instances, serviceId);

		return instances;
	}

	private void addInstancesToList(final List<ServiceInstance> instances, final String serviceId) {
		Response<List<HealthService>> services = client.getHealthServices(serviceId,
				this.properties.isQueryPassing(), QueryParams.DEFAULT);
		for (HealthService service : services.getValue()) {
			String host = findHost(service);
			List<String> tags = service.getService().getTags();
			ConsulServiceInstance serviceInstance = new ConsulServiceInstance(serviceId, host,
			        service.getService().getPort(), false, tags);
			if (filter.accept(serviceInstance)) {
                instances.add(serviceInstance);
            }
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
