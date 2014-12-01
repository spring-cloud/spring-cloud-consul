package org.springframework.cloud.consul.config;

import org.springframework.cloud.consul.client.KeyValueClient;
import org.springframework.cloud.consul.client.NotFoundException;
import org.springframework.cloud.consul.model.KeyValue;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.*;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySource extends EnumerablePropertySource<KeyValueClient> {

    private String context;

    private Map<String, String> properties = new LinkedHashMap<>();

    public ConsulPropertySource(String context, KeyValueClient source) {
        super(context, source);
        this.context = context;

        if (!this.context.endsWith("/")) {
            this.context = this.context + "/";
        }
    }

    public void init() {
        try {
            List<KeyValue> keyValues = source.getKeyValueRecurse(context);

            for (KeyValue keyValue : keyValues) {
                String key = keyValue.getKey()
                    .replace(context, "")
                    .replace('/', '.');
                String value = keyValue.getDecoded();
                properties.put(key, value);
            }
        } catch (NotFoundException e) {
            //not found, do nothing
        }
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(new String[0]);
    }
}
