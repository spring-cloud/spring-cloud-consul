package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Spencer Gibb
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Check {
    @JsonProperty("ID")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("Script")
    private String script;

    @JsonProperty("Interval")
    private String interval;

    @JsonProperty("TTL")
    private String ttl;
}
