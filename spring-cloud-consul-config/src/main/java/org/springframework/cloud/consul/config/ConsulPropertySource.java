/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

import static org.springframework.util.Base64Utils.decodeFromString;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySource extends EnumerablePropertySource<ConsulClient> {

	private String context;
	private ConsulConfigProperties configProperties;

	private final Map<String, String> properties = new LinkedHashMap<>();

	public ConsulPropertySource(String context, ConsulClient source,
			ConsulConfigProperties configProperties) {
		super(context, source);
		this.context = context;
		this.configProperties = configProperties;

		if (!this.context.endsWith("/")) {
			this.context = this.context + "/";
		}
	}

	public void init() {
		Response<List<GetValue>> response = source.getKVValues(context,
				configProperties.getAclToken(), QueryParams.DEFAULT);

		final List<GetValue> values = response.getValue();
		ConsulConfigProperties.Format format = configProperties.getFormat();
		switch (format) {
		case KEY_VALUE:
			parsePropertiesInKeyValueFormat(values);
			break;
		case PROPERTIES:
		case YAML:
			parsePropertiesWithNonKeyValueFormat(values, format);
		}
	}

	/**
	 * Parses the properties in key value style i.e., values are expected to be either a
	 * sub key or a constant
	 *
	 * @param values
	 */
	private void parsePropertiesInKeyValueFormat(List<GetValue> values) {
		if (values == null) {
			return;
		}

		for (GetValue getValue : values) {
			String key = getValue.getKey();
			if (!StringUtils.endsWithIgnoreCase(key, "/")) {
				key = key.replace(context, "").replace('/', '.');
				String value = getDecoded(getValue.getValue());
				properties.put(key, value);
			}
		}
	}

	/**
	 * Parses the properties using the format which is not a key value style i.e., either
	 * java properties style or YAML style
	 *
	 * @param values
	 */
	private void parsePropertiesWithNonKeyValueFormat(List<GetValue> values,
			ConsulConfigProperties.Format format) {
		if (values == null) {
			return;
		}

		for (GetValue getValue : values) {
			String key = getValue.getKey().replace(context, "");
			if (configProperties.getDataKey().equals(key)) {
				final String value = getDecoded(getValue.getValue());
				final Properties props = generateProperties(value, format);

				for (Map.Entry entry : props.entrySet()) {
					properties
							.put(entry.getKey().toString(), entry.getValue().toString());
				}
			}
		}
	}

	private Properties generateProperties(String value,
			ConsulConfigProperties.Format format) {
		final Properties props = new Properties();

		if (format == ConsulConfigProperties.Format.PROPERTIES) {
			try {
				// Must use the ISO-8859-1 encoding because Properties.load(stream)
				// expects it.
				props.load(new ByteArrayInputStream(value.getBytes("ISO-8859-1")));
			}
			catch (IOException e) {
				throw new IllegalArgumentException(value
						+ " can't be encoded using ISO-8859-1");
			}

			return props;
		}
		else if (format == ConsulConfigProperties.Format.YAML) {
			final YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
			yaml.setResources(new ByteArrayResource(value.getBytes()));

			return yaml.getObject();
		}

		return props;
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
