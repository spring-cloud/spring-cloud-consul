package org.springframework.cloud.consul.discovery.filters;

import com.ecwid.consul.v1.health.model.Check;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.cloud.consul.discovery.ConsulServer;

import java.util.ArrayList;
import java.util.List;

public class NonCriticalServerListFilter implements ServerListFilter<Server> {
	@Override
	public List<Server> getFilteredListOfServers(List<Server> input) {
		List<Server> servers = new ArrayList<>(1 + input.size());
        for (Server server : input) {
            if(!cast(server).getLowestCheckStatus().equals(Check.CheckStatus.CRITICAL)){
                servers.add(server);
            }
        }
        return servers;
	}
    private ConsulServer cast(Server server) {
        return ConsulServer.class.cast(server);
    }
}
