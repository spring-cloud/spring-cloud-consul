/*
 * Copyright 2013-2016 the original author or authors.
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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.PROPERTIES;
import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.YAML;

/**
 * @author Spencer Gibb
 */
public class ConsulFilesPropertySource extends ConsulPropertySource {
	public ConsulFilesPropertySource(String context, ConsulClient source, ConsulConfigProperties configProperties) {
		super(context, source, configProperties);
	}

	@Override
	public void init() {
		//noop
	}

	public void init(GetValue value) {
		if (this.getContext().endsWith(".yml") || this.getContext().endsWith(".yaml")) {
			parseValue(value, YAML);
		} else if (this.getContext().endsWith(".properties")) {
			parseValue(value, PROPERTIES);
		} else {
			throw new IllegalStateException("Unknown files extension for context " + this.getContext());
		}
	}
}
