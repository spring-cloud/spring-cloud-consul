package org.springframework.cloud.consul.client;

import feign.RequestLine;
import org.springframework.cloud.consul.model.KeyValue;

import javax.inject.Named;
import java.util.List;

/**
 * @author Spencer Gibb
 */
public interface KeyValueClient {
    @RequestLine("GET /v1/kv/{key}")
    List<KeyValue> getKeyValue(@Named("key") String key);

    @RequestLine("GET /v1/kv/?recurse=true")
    List<KeyValue> getKeyValueRecurse();

    @RequestLine("GET /v1/kv/{key}?recurse=true")
    List<KeyValue> getKeyValueRecurse(@Named("key") String key);

    @RequestLine("GET /v1/kv/?keys=true")
    List<String> getKeys();

    @RequestLine("GET /v1/kv/{key}?keys=true")
    List<String> getKeys(@Named("key") String key);

    @RequestLine("PUT /v1/kv/{key}")
    boolean put(@Named("key") String key, Object value);

    @RequestLine("DELETE /v1/kv/{key}")
    void delete(@Named("key") String key);
}
