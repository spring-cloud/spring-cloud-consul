package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.google.common.base.Throwables;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Spencer Gibb
 */
public class ConsulLoadBalancerClient implements LoadBalancerClient {

    private ConcurrentHashMap<String, ILoadBalancer> namedLoadBalancers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IClientConfig> namedClientConfigs = new ConcurrentHashMap<>();

    @Autowired
    ConsulClient client;

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
                    //FIXME: .withDynamicServerList(new ConsulServerList(catalogClient, serviceId))
                    .buildDynamicServerListLoadBalancer();
            namedLoadBalancers.put(serviceId, lb);
        }
        Server server = lb.chooseServer(null);
        return new DefaultServiceInstance(server.getId(), server.getHost(), server.getPort());
    }

    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) {
        try {
            return request.apply(choose(serviceId));
        } catch (Exception e) {
            Throwables.propagate(e);
            return null;
        }
    }

    @Override
    public URI reconstructURI(ServiceInstance instance, URI original) {
        //TODO: move this pattern to a helper method
        URI uri = UriComponentsBuilder.fromUri(original)
                .host(instance.getHost())
                .port(instance.getPort())
                .build()
                .toUri();
        return uri;
    }
}
