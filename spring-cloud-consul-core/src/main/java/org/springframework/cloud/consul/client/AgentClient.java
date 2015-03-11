package org.springframework.cloud.consul.client;

import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.consul.model.Member;
import org.springframework.cloud.consul.model.Service;

import java.util.List;
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
    void deregister(@Param("serviceId") String serviceId);

    @RequestLine("GET /v1/agent/members")
    List<Member> getMembers();
}
