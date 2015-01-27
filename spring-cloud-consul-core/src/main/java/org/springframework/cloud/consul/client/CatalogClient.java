package org.springframework.cloud.consul.client;

import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.consul.model.ServiceNode;

import java.util.List;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
public interface CatalogClient {
    @RequestLine("GET /v1/catalog/services")
    Map<String, List<String>> getServices();

    @RequestLine("GET /v1/catalog/service/{serviceId}")
    List<ServiceNode> getServiceNodes(@Param("serviceId") String serviceId);
}
