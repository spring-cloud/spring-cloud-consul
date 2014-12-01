package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.io.BaseEncoding.base64;

/**
 * @author Spencer Gibb
 * Example:
 *  "ID": "b54fe110-7af5-cafc-d1fb-afc8ba432b1c",
    "Name": "deploy",
    "Payload": null,
    "NodeFilter": "",
    "ServiceFilter": "",
    "TagFilter": "",
    "Version": 1,
    "LTime": 0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    @JsonProperty("ID")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("NodeFilter")
    private String nodeFilter;

    @JsonProperty("ServiceFilter")
    private String serviceFilter;

    @JsonProperty("TagFilter")
    private String tagFilter;

    @JsonProperty("Version")
    private Long version;

    @JsonProperty("LTime")
    private Long lTime;

    @JsonProperty("Payload")
    private String payload;

    public String getDecoded() {
        if (payload == null)
            return null;
        return new String(base64().decode(payload), UTF_8);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("nodeFilter", nodeFilter)
                .add("serviceFilter", serviceFilter)
                .add("tagFilter", tagFilter)
                .add("version", version)
                .add("lTime", lTime)
                .add("payload", payload)
                .add("decodedPayload", getDecoded())
                .toString();
    }
}
