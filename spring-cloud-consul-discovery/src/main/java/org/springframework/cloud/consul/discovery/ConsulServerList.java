package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.google.common.base.Function;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Spencer Gibb
 */
public class ConsulServerList extends AbstractServerList<ConsulServer> {

    private ConsulClient client;

    private String serviceId;

    public ConsulServerList() {
    }

    public ConsulServerList(ConsulClient client, String serviceId) {
        this.client = client;
        this.serviceId = serviceId;
    }

    public void setClient(ConsulClient client) {
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
        Collection<ConsulServer> servers = transform(response.getValue(), new Function<CatalogService, ConsulServer>() {
            @Nullable
            @Override
            public ConsulServer apply(@Nullable CatalogService service) {
                ConsulServer server = new ConsulServer(service);
                return server;
            }
        });

        return newArrayList(servers);
    }
}
