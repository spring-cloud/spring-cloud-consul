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

import org.springframework.boot.autoconfigure.web.ServerProperties;
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
import com.ecwid.consul.v1.health.model.HealthService;

import static org.springframework.cloud.consul.discovery.ConsulServerUtils.findHost;
import static org.springframework.cloud.consul.discovery.ConsulServerUtils.getMetadata;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Spencer Gibb
 * @author Joe Athman
 */
@CommonsLog
public class ConsulDiscoveryClient implements DiscoveryClient {

	interface LocalResolver {
		String getServiceId();
		Integer getPort();
	}

	private final ConsulClient client;
	private final ConsulDiscoveryProperties properties;
	private final LocalResolver localResolver;

	private ServerProperties serverProperties;

	@Deprecated
	public ConsulDiscoveryClient(ConsulClient client, final ConsulLifecycle lifecycle,
								 ConsulDiscoveryProperties properties) {
		this(client, properties, new LocalResolver() {
			@Override
			public String getServiceId() {
				return lifecycle.getServiceId();
			}

			@Override
			public Integer getPort() {
				return lifecycle.getConfiguredPort();
			}
		});
	}

	public ConsulDiscoveryClient(ConsulClient client, ConsulDiscoveryProperties properties,
				LocalResolver localResolver) {
		this.client = client;
		this.properties = properties;
		this.localResolver = localResolver;
	}

	public void setServerProperties(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public String description() {
		return "Spring Cloud Consul Discovery Client";
	}

	@Override
	public ServiceInstance getLocalServiceInstance() {
		Response<Map<String, Service>> agentServices = client.getAgentServices();
		Service service = agentServices.getValue().get(localResolver.getServiceId());
		String serviceId;
		Integer port;
		Map<String, String> metadata;
		String host = "localhost";

		// if we have a response from consul, that is the ultimate source of truth
		if (service != null) {
			serviceId = service.getId();
			port = service.getPort();
			host = service.getAddress();
			metadata = getMetadata(service.getTags());
		} else {
			// possibly called before registration, use configuration or best guess
			log.warn("Unable to locate service in consul agent: "
					+ localResolver.getServiceId());

			serviceId = localResolver.getServiceId();
			port = localResolver.getPort();
			if (port == 0 && serverProperties != null
					&& serverProperties.getPort() != null) {
				port = serverProperties.getPort();
			}
			metadata = getMetadata(this.properties.getTags());

			if (StringUtils.hasText(this.properties.getHostname())) {
				host = this.properties.getHostname();
			} else if (this.properties.isPreferAgentAddress()){
				// try and use the agent host
				String agentHost = getAgentHost();
				if (agentHost != null) {
					host = agentHost;
				}
			}
		}

		return new DefaultServiceInstance(serviceId, host, port, false, metadata);
	}

	private String getAgentHost() {
		Response<Self> agentSelf = client.getAgentSelf();
		Member member = agentSelf.getValue().getMember();
		if (member != null) {
			if (properties.isPreferIpAddress()) {
				return member.getAddress();
			} else if (StringUtils.hasText(member.getName())) {
				return member.getName();
			}
		}
		return null;
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
		Response<List<HealthService>> services = client.getHealthServices(serviceId,
				this.properties.isQueryPassing(), queryParams);
		for (HealthService service : services.getValue()) {
			String host = findHost(service);
			instances.add(new DefaultServiceInstance(serviceId, host, service
					.getService().getPort(), false, getMetadata(service)));
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
		return new ArrayList<>(client.getCatalogServices(QueryParams.DEFAULT).getValue()
				.keySet());
	}
}
