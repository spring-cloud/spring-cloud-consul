package org.springframework.cloud.consul.discovery;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.cloud.consul.discovery.filters.AliveServerListFilter;
import org.springframework.cloud.consul.discovery.filters.NonCriticalServerListFilter;

import java.util.List;

public class MultipleServerListFilter implements ServerListFilter<Server> {
    private ServerListFilter<Server>[] filters;

    public MultipleServerListFilter(ServerListFilter<Server>... filters) {
        this.filters = filters;
    }

    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
        List<Server> res = servers;
        for (ServerListFilter<Server> filter : filters) {
            res = filter.getFilteredListOfServers(res);
        }
        return res;
    }
}
