package org.springframework.cloud.consul.discovery;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.springframework.cloud.consul.client.CatalogClient;
import org.springframework.cloud.consul.model.ServiceNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Spencer Gibb
 */
public class ConsulServerList extends AbstractServerList<ConsulServer> {

    private CatalogClient client;

    private String serviceId;

    public ConsulServerList() {
    }

    public ConsulServerList(CatalogClient client, String serviceId) {
        this.client = client;
        this.serviceId = serviceId;
    }

    public void setClient(CatalogClient client) {
        this.client = client;
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
        List<ServiceNode> nodes = client.getServiceNodes(serviceId);
        if (nodes == null || nodes.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

		List<ConsulServer> servers = new ArrayList<>();
		for (ServiceNode node : nodes) {
			ConsulServer server = new ConsulServer(node);
			servers.add(server);
		}

        return servers;
    }
}
