/*
 * Copyright 2013-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.retry.annotation.Retryable;

/**
 * @author Spencer Gibb
 */
@Order(0)
public class ConsulPropertySourceLocator implements PropertySourceLocator, ConsulConfigIndexes {

	private static final Log log = LogFactory.getLog(ConsulPropertySourceLocator.class);

	private final ConsulClient consul;

	private final ConsulConfigProperties properties;

	private final List<String> contexts = new ArrayList<>();

	private final LinkedHashMap<String, Long> contextIndex = new LinkedHashMap<>();

	public ConsulPropertySourceLocator(ConsulClient consul, ConsulConfigProperties properties) {
		this.consul = consul;
		this.properties = properties;
	}

	@Deprecated
	public List<String> getContexts() {
		return this.contexts;
	}

	@Override
	public LinkedHashMap<String, Long> getIndexes() {
		return this.contextIndex;
	}

	@Override
	@Retryable(interceptor = "consulRetryInterceptor")
	public Collection<PropertySource<?>> locateCollection(Environment environment) {
		return PropertySourceLocator.locateCollection(this, environment);
	}

	@Override
	@Retryable(interceptor = "consulRetryInterceptor")
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;

			ConsulPropertySources sources = new ConsulPropertySources(properties, log);

			List<String> profiles = Arrays.asList(env.getActiveProfiles());
			this.contexts.addAll(sources.getAutomaticContexts(profiles));

			CompositePropertySource composite = new CompositePropertySource("consul");

			for (String propertySourceContext : this.contexts) {
				ConsulPropertySource propertySource = sources.createPropertySource(propertySourceContext, true,
						this.consul, contextIndex::put);
				if (propertySource != null) {
					composite.addPropertySource(propertySource);
				}
			}

			return composite;
		}
		return null;
	}

	private void addIndex(String propertySourceContext, Long consulIndex) {
		this.contextIndex.put(propertySourceContext, consulIndex);
	}

	private ConsulPropertySource create(String context) {
		ConsulPropertySource propertySource = new ConsulPropertySource(context, this.consul, this.properties);
		propertySource.init();
		addIndex(context, propertySource.getInitialIndex());
		return propertySource;
	}

}
