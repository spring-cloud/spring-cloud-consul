package org.springframework.cloud.consul.discovery;

import com.google.common.base.Function;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.springframework.cloud.consul.client.CatalogClient;
import org.springframework.cloud.consul.model.ServiceNode;

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

    private CatalogClient client;

    private String serviceId;


    public ConsulServerList(CatalogClient client, String serviceId) {
        this.client = client;
        this.serviceId = serviceId;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
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
        List<ServiceNode> nodes = client.getServiceNodes(serviceId);
        if (nodes == null || nodes.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Collection<ConsulServer> servers = transform(nodes, new Function<ServiceNode, ConsulServer>() {
            @Nullable
            @Override
            public ConsulServer apply(@Nullable ServiceNode node) {
                ConsulServer server = new ConsulServer(node);
                return server;
            }
        });

        return newArrayList(servers);
    }
}
