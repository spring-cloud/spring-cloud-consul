package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static org.springframework.util.Base64Utils.*;

/**
 * @author Spencer Gibb
 */
@Data
public class KeyValue {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("CreateIndex")
    private Long createIndex;

    @JsonProperty("ModifyIndex")
    private Long modifyIndex;

    @JsonProperty("Flags")
    private Long flags;

    //TODO: use jackson to do the encoded/decoding
    public String getDecoded() {
        if (value == null)
            return null;
        return new String(decodeFromString(value));
    }

}
