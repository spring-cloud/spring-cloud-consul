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

package org.springframework.cloud.vault.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.core.env.EnumerablePropertySource;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class VaultPropertySource extends EnumerablePropertySource<VaultClient> {

	private String context;

	private Map<String, String> properties = new LinkedHashMap<>();

	public VaultPropertySource(String context, VaultClient source) {
		super(context, source);
		this.context = context;
	}

	public void init() {
		try {
			Map<String, String> values = this.source.read(this.context);

			if (values != null) {
				properties.putAll(values);
			}
		} catch (Exception e) {
			log.error("Unable to read properties from vault for key "+this.context, e);
		}
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
