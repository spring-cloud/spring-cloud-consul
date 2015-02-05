package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static org.springframework.util.Base64Utils.*;

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
        return new String(decodeFromString(payload));
    }

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Event{");
		sb.append("id='").append(id).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", nodeFilter='").append(nodeFilter).append('\'');
		sb.append(", serviceFilter='").append(serviceFilter).append('\'');
		sb.append(", tagFilter='").append(tagFilter).append('\'');
		sb.append(", version=").append(version);
		sb.append(", lTime=").append(lTime);
		sb.append(", payload='").append(payload).append('\'');
		sb.append(", decoded='").append(getDecoded()).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
