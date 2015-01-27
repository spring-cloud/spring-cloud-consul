package org.springframework.cloud.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.catalog.model.Node;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "endpoints.consul", ignoreUnknownFields = false)
public class ConsulEndpoint extends AbstractEndpoint<ConsulEndpoint.ConsulData> {

    @Autowired
    private ConsulClient consul;

    @Autowired
    public ConsulEndpoint() {
        super("consul", false, true);
    }

    @Override
    public ConsulData invoke() {
        ConsulData data = new ConsulData();
        //data.setKeyValues(kvClient.getKeyValueRecurse());
        Response<Map<String, Service>> agentServices = consul.getAgentServices();
        data.setAgentServices(agentServices.getValue());

        Response<Map<String, List<String>>> catalogServices = consul.getCatalogServices(QueryParams.DEFAULT);


        for (String serviceId : catalogServices.getValue().keySet()) {
            Response<List<CatalogService>> response = consul.getCatalogService(serviceId, QueryParams.DEFAULT);
            data.getCatalogServices().put(serviceId, response.getValue());
        }

        Response<List<Node>> catalogNodes = consul.getCatalogNodes(QueryParams.DEFAULT);
        data.setCatalogNodes(catalogNodes.getValue());

        return data;
    }

    @Data
    public static class ConsulData {
        Map<String, List<CatalogService>> catalogServices = new LinkedHashMap<>();

        Map<String, Service> agentServices;

        List<Node> catalogNodes;

        //List<KeyValue> keyValues;
    }
}
