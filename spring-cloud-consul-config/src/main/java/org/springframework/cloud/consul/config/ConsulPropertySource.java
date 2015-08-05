package org.springframework.cloud.consul.config;

import static org.springframework.util.Base64Utils.decodeFromString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import java.util.*;

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
				String key = getValue.getKey();
				if (!StringUtils.endsWithIgnoreCase(key, "/")) {
					key = key.replace(context, "").replace('/', '.');
					String value = getDecoded(getValue.getValue());
					properties.put(key, value);
				}
			}
		}
	}

	public String getDecoded(String value) {
		if (value == null)
			return null;
		return new String(decodeFromString(value));
	}

	@SuppressWarnings("hiding")
	@Override
	public Object getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public String[] getPropertyNames() {
		Set<String> strings = properties.keySet();
		return strings.toArray(new String[strings.size()]);
	}

	public String getContext() {
		return context;
	}
}
