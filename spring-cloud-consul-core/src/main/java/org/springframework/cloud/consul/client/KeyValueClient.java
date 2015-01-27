package org.springframework.cloud.consul.client;

import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.consul.model.KeyValue;

import java.util.List;

/**
 * @author Spencer Gibb
 */
public interface KeyValueClient {
    @RequestLine("GET /v1/kv/{key}")
    List<KeyValue> getKeyValue(@Param("key") String key);

    @RequestLine("GET /v1/kv/?recurse=true")
    List<KeyValue> getKeyValueRecurse();

    @RequestLine("GET /v1/kv/{key}?recurse=true")
    List<KeyValue> getKeyValueRecurse(@Param("key") String key);

    @RequestLine("GET /v1/kv/?keys=true")
    List<String> getKeys();

    @RequestLine("GET /v1/kv/{key}?keys=true")
    List<String> getKeys(@Param("key") String key);

    @RequestLine("PUT /v1/kv/{key}")
    boolean put(@Param("key") String key, Object value);

    @RequestLine("DELETE /v1/kv/{key}")
    void delete(@Param("key") String key);
}
