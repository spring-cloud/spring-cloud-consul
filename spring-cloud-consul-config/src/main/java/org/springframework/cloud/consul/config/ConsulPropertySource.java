package org.springframework.cloud.consul.config;

import static org.springframework.util.Base64Utils.decodeFromString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySource extends EnumerablePropertySource<ConsulClient> {

	private String context;
	private String aclToken;

	private Map<String, String> properties = new LinkedHashMap<>();

	public ConsulPropertySource(String context, ConsulClient source, String aclToken) {
		super(context, source);
		this.context = context;
		this.aclToken = aclToken;

		if (!this.context.endsWith("/")) {
			this.context = this.context + "/";
		}
	}

	public void init() {
		Response<List<GetValue>> response;
		if (aclToken == null) {
			response = source.getKVValues(context, QueryParams.DEFAULT);
		} else {
			response = source.getKVValues(context, aclToken, QueryParams.DEFAULT);
		}
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

	@Override
	public Object getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public String[] getPropertyNames() {
		Set<String> strings = properties.keySet();
		return strings.toArray(new String[strings.size()]);
	}
}
