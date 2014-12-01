package org.springframework.cloud.consul.discovery;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.client.AgentClient;
import org.springframework.cloud.consul.client.CatalogClient;
import org.springframework.cloud.consul.model.Service;
import org.springframework.cloud.consul.model.ServiceNode;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.*;

/**
 * @author Spencer Gibb
 */
public class ConsulDiscoveryClient implements DiscoveryClient {

    @Autowired
    ApplicationContext context;

    @Autowired
    AgentClient agentClient;

    @Autowired
    CatalogClient catalogClient;

    @Override
    public ServiceInstance getLocalServiceInstance() {
        Map<String, Service> services = agentClient.getServices();
        Service service = services.get(context.getId());
        if (service == null) {
            throw new IllegalStateException("Unable to locate service in consul agent: "+context.getId());
        }
        String host = "localhost";
        Map<String, Object> self = agentClient.getSelf();
        Map<String, Object> member = (Map<String, Object>) self.get("Member");
        if (member != null) {
            if (member.containsKey("Name")) {
                host = (String) member.get("Name");
            }
        }
        return new DefaultServiceInstance(service.getId(), host, service.getPort());
    }

    @Override
    public List<ServiceInstance> getInstances(final String serviceId) {
        List<ServiceNode> nodes = catalogClient.getServiceNodes(serviceId);
        Iterable<ServiceInstance> instances = transform(nodes, new Function<ServiceNode, ServiceInstance>() {
            @Nullable
            @Override
            public ServiceInstance apply(@Nullable ServiceNode node) {
                return new DefaultServiceInstance(serviceId, node.getNode(), node.getServicePort());
            }
        });

        return Lists.newArrayList(instances);
    }

    @Override
    public List<ServiceInstance> getAllInstances() {
        Iterable<ServiceInstance> instances = transform(concat(transform(catalogClient.getServices().keySet(), new Function<String, List<ServiceNode>>() {
            @Nullable
            @Override
            public List<ServiceNode> apply(@Nullable String input) {
                return catalogClient.getServiceNodes(input);
            }
        })), new Function<ServiceNode, ServiceInstance>() {
            @Nullable
            @Override
            public ServiceInstance apply(@Nullable ServiceNode input) {
                return new DefaultServiceInstance(input.getServiceName(), input.getNode(), input.getServicePort());
            }
        });

        return Lists.newArrayList(instances);
    }

    @Override
    public List<String> getServices() {
        return Lists.newArrayList(catalogClient.getServices().keySet());
    }
}
