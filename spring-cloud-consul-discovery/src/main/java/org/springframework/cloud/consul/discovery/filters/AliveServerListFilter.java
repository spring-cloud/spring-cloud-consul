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
import java.util.List;
import java.util.Set;

import org.springframework.cloud.consul.discovery.ConsulServer;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;

/**
 * Server filter: returns only alive servers. Each consul agent runs a serf agent which is
 * a member of the serf gossip pool. The serf status (alive/failed/etc) is reflected in 2
 * consul APIs: in the agent API and in the catalog API. We prefer the agent API because
 * it is most up to date (or perhaps we should intersect them and pick members that are
 * live in both).
 * @author nicu marasoiu on 10.03.2015.
 */
@Deprecated
public class AliveServerListFilter implements ServerListFilter<Server> {
	private FilteringAgentClient filteringAgentClient;

	public AliveServerListFilter(FilteringAgentClient filteringAgentClient) {
		this.filteringAgentClient = filteringAgentClient;
	}

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		Set<String> liveNodes = filteringAgentClient.getAliveAgentsAddresses();
		List<Server> filteredServers = new ArrayList<>();
		for (Server server : servers) {
			ConsulServer consulServer = ConsulServer.class.cast(server);
			if (liveNodes.contains(consulServer.getHealthService().getService().getAddress())) {
				filteredServers.add(server);
			}
		}
		return filteredServers;
	}
}
