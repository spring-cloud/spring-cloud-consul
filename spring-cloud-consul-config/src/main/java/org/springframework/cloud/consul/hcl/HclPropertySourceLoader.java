/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.hcl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import com.bertramlabs.plugins.hcl4j.HCLParserException;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public class HclPropertySourceLoader implements PropertySourceLoader {

	@Override
	public String[] getFileExtensions() {
		return new String[] {"hcl"};
	}

	@Override
	public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
		Map<String, ?> properties = loadProperties(resource);
		if (properties.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections
			.singletonList(new OriginTrackedMapPropertySource(name, Collections
				.unmodifiableMap(properties), true));

	}

	private Map<String, ?> loadProperties(Resource resource) throws IOException {
		try {
			Map<String, Object> map = new HCLParser().parse(resource.getInputStream());
			map = getFlattenedMap(map);
			return map;
		}
		catch (HCLParserException e) {
			throw new IOException("Error parsing " + resource.getFilename(), e);
		}
	}

	/**
	 * Return a flattened version of the given map, recursively following any nested Map
	 * or Collection values. Entries from the resulting map retain the same order as the
	 * source. When called with the Map from a {@link YamlProcessor.MatchCallback} the result will
	 * contain the same values as the {@link YamlProcessor.MatchCallback} Properties.
	 * @param source the source map
	 * @return a flattened map
	 * @since 4.1.3
	 */
	protected final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
		Map<String, Object> result = new LinkedHashMap<>();
		buildFlattenedMap(result, source, null);
		return result;
	}

	private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, @Nullable String path) {
		source.forEach((key, value) -> {
			if (StringUtils.hasText(path)) {
				if (key.startsWith("[")) {
					key = path + key;
				}
				else {
					key = path + '.' + key;
				}
			}
			if (value instanceof String) {
				result.put(key, value);
			}
			else if (value instanceof Map) {
				// Need a compound key
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) value;
				buildFlattenedMap(result, map, key);
			}
			else if (value instanceof Collection) {
				// Need a compound key
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) value;
				if (collection.isEmpty()) {
					result.put(key, "");
				}
				else {
					int count = 0;
					for (Object object : collection) {
						buildFlattenedMap(result, Collections.singletonMap(
							"[" + (count++) + "]", object), key);
					}
				}
			}
			else {
				result.put(key, (value != null ? value : ""));
			}
		});
	}
}
