package org.springframework.cloud.consul.discovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.client.AgentClient;
import org.springframework.cloud.consul.client.CatalogClient;
import org.springframework.cloud.consul.model.Service;
import org.springframework.cloud.consul.model.ServiceNode;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public String description() {
        return "Spring Cloud Consul Discovery Client";
    }

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
        return new DefaultServiceInstance(service.getId(), host, service.getPort(), false);
    }

    @Override
    public List<ServiceInstance> getInstances(final String serviceId) {
        List<ServiceNode> nodes = catalogClient.getServiceNodes(serviceId);
		List<ServiceInstance> instances = new ArrayList<>();
		for (ServiceNode node : nodes) {
            instances.add(new DefaultServiceInstance(serviceId, node.getNode(), node.getServicePort(), false));
		}

        return instances;
    }

    public List<ServiceInstance> getAllInstances() {
        List<ServiceInstance> instances = new ArrayList<>();

		for (String serviceId : catalogClient.getServices().keySet()) {
			List<ServiceNode> serviceNodes = catalogClient.getServiceNodes(serviceId);
			if (serviceNodes != null) {
				for (ServiceNode node : serviceNodes) {
					instances.add(new DefaultServiceInstance(node.getServiceName(), node.getNode(), node.getServicePort(), false));
				}
			}
		}
		return instances;
    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(catalogClient.getServices().keySet());
    }
}
