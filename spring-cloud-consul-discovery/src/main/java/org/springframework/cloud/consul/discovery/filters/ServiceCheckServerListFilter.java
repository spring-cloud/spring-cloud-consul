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

package org.springframework.cloud.consul.discovery.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.consul.discovery.ConsulServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.health.model.Check;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;

/**
 * Created by nicu on 12.03.2015.
 */
public class ServiceCheckServerListFilter implements ServerListFilter<Server> {

	private ConsulClient client;

	public ServiceCheckServerListFilter(ConsulClient client) {
		this.client = client;
	}

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		Set<String> passingServiceIds = getPassingServiceIds(servers);
		List<Server> okServers = new ArrayList<>(servers.size());
		for (Server server : servers) {
			String serviceId = server.getMetaInfo().getInstanceId();
			if (passingServiceIds.contains(serviceId)) {
				ConsulServer consulServer = ConsulServer.class.cast(server);
				List<Check> nodeChecks = client.getHealthChecksForNode(
						consulServer.getNode(), QueryParams.DEFAULT).getValue();
				boolean passingNodeChecks = true;
				for (Check check : nodeChecks) {
					if (check.getStatus() != Check.CheckStatus.PASSING) {
						passingNodeChecks = false;
						break;
					}
				}
				if (passingNodeChecks) {
					okServers.add(server);
				}
			}
		}
		return okServers;
	}

	private Set<String> getPassingServiceIds(List<Server> servers) {
		Set<String> serviceIds = new HashSet<>(1);
		for (Server server : servers) {
			serviceIds.add(server.getMetaInfo().getInstanceId());
		}
		for (String serviceId : serviceIds) {
			List<Check> serviceChecks = client.getHealthChecksForService(serviceId,
					QueryParams.DEFAULT).getValue();
			for (Check check : serviceChecks) {
				if (check.getStatus() != Check.CheckStatus.PASSING) {
					serviceIds.remove(check.getServiceId());
				}
			}
		}
		return serviceIds;
	}

}
