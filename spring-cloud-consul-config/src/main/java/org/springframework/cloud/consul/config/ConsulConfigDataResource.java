/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.consul.config;

import java.util.Objects;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.core.style.ToStringCreator;

public class ConsulConfigDataResource extends ConfigDataResource {

	private final ConsulConfigProperties properties;

	private final String context;

	private final boolean optional;

	private final ConsulPropertySources consulPropertySources;

	public ConsulConfigDataResource(String context, boolean optional, ConsulConfigProperties properties,
			ConsulPropertySources consulPropertySources) {
		this.properties = properties;
		this.context = context;
		this.optional = optional;
		this.consulPropertySources = consulPropertySources;
	}

	public String getContext() {
		return this.context;
	}

	public boolean isOptional() {
		return this.optional;
	}

	public ConsulConfigProperties getProperties() {
		return this.properties;
	}

	public ConsulPropertySources getConsulPropertySources() {
		return this.consulPropertySources;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConsulConfigDataResource that = (ConsulConfigDataResource) o;
		return this.optional == that.optional && this.context.equals(that.context);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.context, this.optional);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("context", context).append("optional", optional)
				.append("properties", properties).toString();

	}

}
