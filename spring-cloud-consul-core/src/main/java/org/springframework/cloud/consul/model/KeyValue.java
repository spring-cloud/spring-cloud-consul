package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.io.BaseEncoding.base64;

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
        return new String(base64().decode(value), UTF_8);
    }

    public void setUnencoded(String unencoded) {
        setValue(base64().encode(unencoded.getBytes(UTF_8)));
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("key", key)
                .add("value", value)
                .add("decodedValue", getDecoded())
                .add("createIndex", createIndex)
                .add("modifyIndex", modifyIndex)
                .add("flags", flags)
                .toString();
    }
}
