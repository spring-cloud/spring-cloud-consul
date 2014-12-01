package org.springframework.cloud.consul.client;

import feign.RequestLine;
import org.springframework.cloud.consul.model.Service;

import javax.inject.Named;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
public interface AgentClient {
    @RequestLine("GET /v1/agent/services")
    Map<String, Service> getServices();

    @RequestLine("GET /v1/agent/self")
    //TODO change map to an object
    Map<String, Object> getSelf();

    @RequestLine("PUT /v1/agent/service/register")
    void register(Service service);

    @RequestLine("PUT /v1/agent/service/deregister/{serviceId}")
    void deregister(@Named("serviceId") String serviceId);
}
