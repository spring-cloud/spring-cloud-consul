package org.springframework.cloud.consul;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.consul.client.AgentClient;
import org.springframework.cloud.consul.client.CatalogClient;
import org.springframework.cloud.consul.client.KeyValueClient;
import org.springframework.cloud.consul.model.KeyValue;
import org.springframework.cloud.consul.model.Service;
import org.springframework.cloud.consul.model.ServiceNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "endpoints.consul", ignoreUnknownFields = false)
public class ConsulEndpoint extends AbstractEndpoint<ConsulEndpoint.ConsulData> {

    @Autowired
    KeyValueClient keyValueClient;

    @Autowired
    CatalogClient catalogClient;

    @Autowired
    AgentClient agentClient;

    @Autowired
    public ConsulEndpoint() {
        super("consul", false, true);
    }

    @Override
    public ConsulData invoke() {
        ConsulData data = new ConsulData();
        //data.setKeyValues(kvClient.getKeyValueRecurse());
        data.setCatalogServices(catalogClient.getServices());
        Map<String, Service> services = agentClient.getServices();
        data.setAgentServices(services);

        for (String serviceId : services.keySet()) {
            data.getCatalogServiceNodes().put(serviceId, catalogClient.getServiceNodes(serviceId));
        }

        return data;
    }

    @Data
    public static class ConsulData {
        Map<String, List<String>> catalogServices;

        Map<String, List<ServiceNode>> catalogServiceNodes = new LinkedHashMap<>();

        Map<String, Service> agentServices;

        List<KeyValue> keyValues;
    }
}
