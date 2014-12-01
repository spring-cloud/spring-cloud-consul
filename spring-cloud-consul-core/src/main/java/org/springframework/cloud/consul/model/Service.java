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
public class Service {
    @JsonProperty("ID")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Tags")
    private List<String> tags;

    @JsonProperty("Port")
    private int port;

    @JsonProperty("Check")
    private Check check;
}
