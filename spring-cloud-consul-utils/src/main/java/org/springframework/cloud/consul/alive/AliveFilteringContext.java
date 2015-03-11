package org.springframework.cloud.consul.alive;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.discovery.ConsulServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects a server list filter for giving servers hosting a service only for the live servers per serf status.
 * @author nicu marasoiu on 10.03.2015.
 */
@Configuration
@ComponentScan
public class AliveFilteringContext {
    @Bean
    @Autowired
    public ServerListFilter<Server> aliveServerListFilter(FilteringAgentClient filteringAgentClient) {
        return adapter(new AliveServerListFilter(filteringAgentClient));
    }

    private ServerListFilter<Server> adapter(final ServerListFilter<ConsulServer> consulServerList) {

        return new ServerListFilter<Server>() {
            @Override
            public List<Server> getFilteredListOfServers(List<Server> servers) {
                return adapt2(consulServerList.getFilteredListOfServers(adapt1(servers)));
            }

            private List<ConsulServer> adapt1(List<Server> servers) {
                List<ConsulServer> consulServers = new ArrayList<ConsulServer>(servers.size());
                for (Server consulServer : servers) {
                    consulServers.add((ConsulServer) consulServer);
                }
                return consulServers;
            }

            private List<Server> adapt2(List<ConsulServer> consulServers) {
                List<Server> servers = new ArrayList<Server>(consulServers.size());
                for (ConsulServer consulServer : consulServers) {
                    servers.add(consulServer);
                }
                return servers;
            }
        };
    }
}
