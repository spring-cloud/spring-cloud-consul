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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author Spencer Gibb
 */
public class VaultPropertySourceLocator implements PropertySourceLocator {

	private VaultClient vault;

	private VaultConfigProperties properties;

	public VaultPropertySourceLocator(VaultClient vault, VaultConfigProperties properties) {
		this.vault = vault;
		this.properties = properties;
	}

	@Override
	public PropertySource<?> locate(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
			String appName = env.getProperty("spring.application.name");
			List<String> profiles = Arrays.asList(env.getActiveProfiles());

			List<String> contexts = new ArrayList<>();

			String defaultContext = this.properties.getDefaultContext();
			contexts.add(defaultContext);
			addProfiles(contexts, defaultContext, profiles);

			String baseContext = appName;
			contexts.add(baseContext);
			addProfiles(contexts, baseContext, profiles);

			Collections.reverse(contexts);

			CompositePropertySource composite = new CompositePropertySource("vault");

			for (String propertySourceContext : contexts) {
				VaultPropertySource propertySource = create(propertySourceContext);
				propertySource.init();
				composite.addPropertySource(propertySource);
			}

			return composite;
		}
		return null;
	}

	private VaultPropertySource create(String context) {
		return new VaultPropertySource(context, vault);
	}

	private void addProfiles(List<String> contexts, String baseContext,
			List<String> profiles) {
		for (String profile : profiles) {
			contexts.add(baseContext + this.properties.getProfileSeparator() + profile);
		}
	}
}
