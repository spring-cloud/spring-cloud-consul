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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceLocator implements PropertySourceLocator {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulProperties properties;

	@Override
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
			String appName = env.getProperty("spring.application.name");
			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			String prefix = properties.getPrefix();
			List<String> contexts = new ArrayList<>();

			String defaultContext = prefix + "/application";
			contexts.add(defaultContext + "/");
			addProfiles(contexts, defaultContext, profiles);

			String baseContext = prefix + "/" + appName;
			contexts.add(baseContext + "/");
			addProfiles(contexts, baseContext, profiles);

			CompositePropertySource composite = new CompositePropertySource("consul");

			for (String propertySourceContext : contexts) {
				ConsulPropertySource propertySource = create(propertySourceContext);
				propertySource.init();
				composite.addPropertySource(propertySource);
			}

			return composite;
		}
		return null;
	}

	private ConsulPropertySource create(String context) {
		return new ConsulPropertySource(context, consul);
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles) {
		for (String profile : profiles) {
			contexts.add(baseContext + "::" + profile + "/");
		}
	}
}
