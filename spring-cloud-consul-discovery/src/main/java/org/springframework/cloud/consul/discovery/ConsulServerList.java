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
import java.util.Collections;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

/**
 * @author Spencer Gibb
 */
public class ConsulServerList extends AbstractServerList<ConsulServer> {

	private final ConsulClient client;
	private ConsulDiscoveryProperties properties;

	private String serviceId;

	public ConsulServerList(ConsulClient client, ConsulDiscoveryProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig clientConfig) {
		this.serviceId = clientConfig.getClientName();
	}

	@Override
	public List<ConsulServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<ConsulServer> getUpdatedListOfServers() {
		return getServers();
	}

	private List<ConsulServer> getServers() {
		if (client == null) {
			return Collections.emptyList();
		}
		Response<List<CatalogService>> response = client.getCatalogService(
				this.serviceId, QueryParams.DEFAULT);
		if (response.getValue() == null || response.getValue().isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		ArrayList<ConsulServer> servers = new ArrayList<>();
		for (CatalogService service : response.getValue()) {
			servers.add(new ConsulServer(service, properties.isPreferIpAddress()));
		}
		return servers;
	}
}
