package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Spencer Gibb
 */
public class ConsulServerList extends AbstractServerList<ConsulServer> {

    private final ConsulClient client;

    private String serviceId;

    public ConsulServerList(ConsulClient client) {
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
        Response<List<CatalogService>> response = client.getCatalogService(this.serviceId, QueryParams.DEFAULT);
        if (response.getValue() == null || response.getValue().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<ConsulServer> servers = new ArrayList<>();
		for (CatalogService service : response.getValue()) {
			servers.add(new ConsulServer(service));
		}
        return servers;
    }
}
