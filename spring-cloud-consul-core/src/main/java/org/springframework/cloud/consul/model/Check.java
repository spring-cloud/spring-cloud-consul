package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Spencer Gibb
 */
@Data
public class Check {
    @JsonProperty("Script")
    private String script;

    @JsonProperty("Interval")
    private int interval;

    @JsonProperty("TTL")
    private int ttl;
}
