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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.retry.annotation.Retryable;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Order(0)
public class ConsulPropertySourceLocator implements PropertySourceLocator {

	private ConsulClient consul;

	private ConsulConfigProperties properties;

	private List<String> contexts = new ArrayList<>();

	public ConsulPropertySourceLocator(ConsulClient consul, ConsulConfigProperties properties) {
		this.consul = consul;
		this.properties = properties;
	}

	public List<String> getContexts() {
		return contexts;
	}

	@Override
	@Retryable(interceptor = "consulRetryInterceptor")
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
			String appName = env.getProperty("spring.application.name");
			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			String prefix = this.properties.getPrefix();

			String defaultContext = prefix + "/" + this.properties.getDefaultContext();
			this.contexts.add(defaultContext + "/");
			addProfiles(this.contexts, defaultContext, profiles);

			String baseContext = prefix + "/" + appName;
			this.contexts.add(baseContext + "/");
			addProfiles(this.contexts, baseContext, profiles);

			CompositePropertySource composite = new CompositePropertySource("consul");

			Collections.reverse(this.contexts);

			for (String propertySourceContext : this.contexts) {
				ConsulPropertySource propertySource = create(propertySourceContext);
				propertySource.init();
				composite.addPropertySource(propertySource);
			}

			return composite;
		}
		return null;
	}

	private ConsulPropertySource create(String context) {
		return new ConsulPropertySource(context, consul, properties);
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles) {
		for (String profile : profiles) {
			contexts.add(baseContext + this.properties.getProfileSeparator() + profile + "/");
		}
	}
}
