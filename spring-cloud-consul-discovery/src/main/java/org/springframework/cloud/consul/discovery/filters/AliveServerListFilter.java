package org.springframework.cloud.consul.discovery.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.consul.discovery.ConsulServer;

import com.netflix.loadbalancer.ServerListFilter;

/**
 * Server filter: returns only alive servers. Each consul agent runs a serf agent which is
 * a member of the serf gossip pool. The serf status (alive/failed/etc) is reflected in 2
 * consul APIs: in the agent API and in the catalog API. We prefer the agent API because
 * it is most up to date (or perhaps we should intersect them and pick members that are
 * liv in both).
 * @author nicu marasoiu on 10.03.2015.
 */
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
			if (liveNodes.contains(consulServer.getAddress())) {
				filteredServers.add(server);
			}
		}
		return filteredServers;
	}
}
