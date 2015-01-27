package org.springframework.cloud.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.*;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.BaseEncoding.base64;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySource extends EnumerablePropertySource<ConsulClient> {

    private String context;

    private Map<String, String> properties = new LinkedHashMap<>();

    public ConsulPropertySource(String context, ConsulClient source) {
        super(context, source);
        this.context = context;

        if (!this.context.endsWith("/")) {
            this.context = this.context + "/";
        }
    }

    public void init() {
		Response<List<GetValue>> response = source.getKVValues(context, QueryParams.DEFAULT);
		List<GetValue> values = response.getValue();

        if (values != null) {
            for (GetValue getValue : values) {
                String key = getValue.getKey()
                        .replace(context, "")
                        .replace('/', '.');
                String value = getDecoded(getValue.getValue());
                properties.put(key, value);
            }
        }
    }

    public String getDecoded(String value) {
        if (value == null)
            return null;
        return new String(base64().decode(value), UTF_8);
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
