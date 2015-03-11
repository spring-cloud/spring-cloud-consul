package org.springframework.cloud.consul.alive;

import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.discovery.ConsulServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Server filter: returns only alive servers.
 * Each consul agent runs a serf agent which is a member of the serf gossip pool.
 * The serf status (alive/failed/etc) is reflected in 2 consul APIs: in the agent API and in the catalog API.
 * We prefer the agent API because it is most up to date (or perhaps we should intersect them and pick members that are liv in both).
 * @author nicu marasoiu on 10.03.2015.
 */
public class AliveServerListFilter implements ServerListFilter<ConsulServer>{
    private FilteringAgentClient filteringAgentClient;

    @Autowired
    public AliveServerListFilter(FilteringAgentClient filteringAgentClient) {
        this.filteringAgentClient = filteringAgentClient;
    }

    @Override
    public List<ConsulServer> getFilteredListOfServers(List<ConsulServer> servers) {
        Set<String> liveNodes = filteringAgentClient.getAliveAgentsAddresses();
        List<ConsulServer> filteredServers = new ArrayList<>();
        for (ConsulServer server : servers) {
            if (liveNodes.contains(server.getAddress())) {
                filteredServers.add(server);
            }
        }
        return filteredServers;
    }
}
