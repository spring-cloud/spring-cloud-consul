package org.springframework.cloud.consul.discovery;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.consul.client.CatalogClient;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Spencer Gibb
 */
public class ConsulLoadBalancerClient implements LoadBalancerClient {

    private ConcurrentHashMap<String, ILoadBalancer> namedLoadBalancers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IClientConfig> namedClientConfigs = new ConcurrentHashMap<>();

    @Autowired
    CatalogClient catalogClient;

    @Override
    public ServiceInstance choose(String serviceId) {
        ILoadBalancer lb = namedLoadBalancers.get(serviceId);
        if (lb == null) {
            IClientConfig config = namedClientConfigs.get(serviceId);

            if (config == null) {
                DefaultClientConfigImpl clientConfig = new DefaultClientConfigImpl();
                clientConfig.setClientName(serviceId);
                config = clientConfig;
                namedClientConfigs.put(serviceId, clientConfig);
            }
            lb = LoadBalancerBuilder.<ConsulServer>newBuilder()
                    .withClientConfig(config)
                    //TODO: config to choose rules
                    .withRule(new AvailabilityFilteringRule())
                    //TODO: figure out ping
                    //.withPing()
                    .withDynamicServerList(new ConsulServerList(catalogClient, serviceId))
                    .buildDynamicServerListLoadBalancer();
            namedLoadBalancers.put(serviceId, lb);
        }
        Server server = lb.chooseServer(null);
        return new DefaultServiceInstance(server.getId(), server.getHost(), server.getPort());
    }

    @Override
    public <T> T choose(String serviceId, LoadBalancerRequest<T> request) {
        return request.apply(choose(serviceId));
    }
}
