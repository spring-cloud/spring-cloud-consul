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
import org.springframework.util.ReflectionUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import lombok.extern.apachecommons.CommonsLog;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * @author Spencer Gibb
 */
@Order(0)
@CommonsLog
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

			List<String> suffixes = new ArrayList<>();
			if (this.properties.getFormat() != FILES) {
				suffixes.add("/");
			} else {
				suffixes.add(".yml");
				suffixes.add(".yaml");
				suffixes.add(".properties");
			}

			String defaultContext = prefix + "/" + this.properties.getDefaultContext();
			for (String suffix : suffixes) {
				this.contexts.add(defaultContext + suffix);
			}
			for (String suffix : suffixes) {
				addProfiles(this.contexts, defaultContext, profiles, suffix);
			}

			String baseContext = prefix + "/" + appName;
			for (String suffix : suffixes) {
				this.contexts.add(baseContext + suffix);
			}
			for (String suffix : suffixes) {
				addProfiles(this.contexts, baseContext, profiles, suffix);
			}

			Collections.reverse(this.contexts);

			CompositePropertySource composite = new CompositePropertySource("consul");

			for (String propertySourceContext : this.contexts) {
				try {
					ConsulPropertySource propertySource = null;
					if (this.properties.getFormat() == FILES) {
						Response<GetValue> response = this.consul.getKVValue(propertySourceContext, this.properties.getAclToken());
						if (response.getValue() != null) {
							ConsulFilesPropertySource filesPropertySource = new ConsulFilesPropertySource(propertySourceContext, this.consul, this.properties);
							filesPropertySource.init(response.getValue());
							propertySource = filesPropertySource;
						}
					} else {
						propertySource = create(propertySourceContext);
					}
					if (propertySource != null) {
						composite.addPropertySource(propertySource);
					}
				} catch (Exception e) {
					if (this.properties.isFailFast()) {
						log.error("Fail fast is set and there was an error reading configuration from consul.");
						ReflectionUtils.rethrowRuntimeException(e);
					} else {
						log.warn("Unable to load consul config from "+ propertySourceContext, e);
					}
				}
			}

			return composite;
		}
		return null;
	}

	private ConsulPropertySource create(String context) {
		ConsulPropertySource propertySource = new ConsulPropertySource(context, this.consul, this.properties);
		propertySource.init();
		return propertySource;
	}

	private void addProfiles(List<String> contexts, String baseContext,
							 List<String> profiles, String suffix) {
		for (String profile : profiles) {
			contexts.add(baseContext + this.properties.getProfileSeparator() + profile + suffix);
		}
	}
}
