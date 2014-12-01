package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Spencer Gibb
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceNode {
    @JsonProperty("Node")
    private String node;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("ServiceID")
    private String serviceID;

    @JsonProperty("ServiceName")
    private String serviceName;

    @JsonProperty("ServiceTags")
    private List<String> serviceTags;

    @JsonProperty("ServicePort")
    private int servicePort;
}
