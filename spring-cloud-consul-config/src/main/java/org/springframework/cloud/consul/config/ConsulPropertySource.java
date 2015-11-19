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

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySource extends EnumerablePropertySource<ConsulClient> {

	private String context;
	private ConsulConfigProperties consulConfigProperties;

	private final Map<String, String> properties = new LinkedHashMap<>();

	public ConsulPropertySource(String context,
										 ConsulClient source,
										 ConsulConfigProperties consulConfigProperties) {
		super(context, source);
		this.context = context;
		this.consulConfigProperties = consulConfigProperties;

		if (!this.context.endsWith("/")) {
			this.context = this.context + "/";
		}
	}

	public void init() {
		Response<List<GetValue>> response;
		if (consulConfigProperties.getAclToken() == null) {
			response = source.getKVValues(context, QueryParams.DEFAULT);
		} else {
			response = source.getKVValues(context,
													consulConfigProperties.getAclToken(),
													QueryParams.DEFAULT);
		}

		final List<GetValue> values = response.getValue();
      final ConsulConfigFormat consulConfigFormat =
         ConsulConfigFormat.fromString(consulConfigProperties.getConsulConfigFormat());
      if (consulConfigFormat == ConsulConfigFormat.KEY_VALUE) {
         parsePropertiesInKeyValueFormat(values);
      } else if (consulConfigFormat == ConsulConfigFormat.PROPERTIES) {
         parsePropertiesInPropertiesFormat(values);
      }
	}

   /**
    * Parses the properties in key value style i.e., values are expected to be either a sub key or a
    * constant
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
    * Parses the properties in key value style i.e., values are expected to be either a sub key or a
    * constant
    *
    * @param values
    */
   private void parsePropertiesInPropertiesFormat(List<GetValue> values) {
      if (values == null) {
         return;
      }

      for (GetValue getValue : values) {
         String key = getValue.getKey().replace(context, "");
         if (!consulConfigProperties.getConsulConfigPropertiesKey().equals(key)) {
            continue;
         }
         final String value = getDecoded(getValue.getValue());
         // values should be key=value\nkey1=value....
         final String[] propertyLines = value.split("\n");

         for (String propertyLine : propertyLines) {

            if (StringUtils.isEmpty(propertyLine)
                || propertyLine.trim().length() == 0
                || propertyLine.trim().startsWith("#")) {
               continue;
            }

            propertyLine = propertyLine.trim();

            // property line should be of format key=value
            String[] keyValuePair = propertyLine.split("=");

            if (keyValuePair.length != 2
                || StringUtils.isEmpty(keyValuePair[0])
                || keyValuePair[0].trim().length() == 0
               ) {
               // property line is not of format key=value so ignoring it
               continue;
            }

            properties.put(keyValuePair[0], keyValuePair[1]);
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
