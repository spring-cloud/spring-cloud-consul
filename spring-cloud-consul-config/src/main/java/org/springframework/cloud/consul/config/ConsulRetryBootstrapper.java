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

package org.springframework.cloud.consul.config;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.RetryProperties;
import org.springframework.cloud.consul.config.ConsulBootstrapper.LoaderInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ClassUtils;

/**
 * Consul Retry Bootstrapper.
 *
 * @author Spencer Gibb
 * @since 3.0.2
 */
public class ConsulRetryBootstrapper implements BootstrapRegistryInitializer {

	static final boolean RETRY_IS_PRESENT = ClassUtils.isPresent("org.springframework.retry.annotation.Retryable",
			null);

	@Override
	public void initialize(BootstrapRegistry registry) {
		if (!RETRY_IS_PRESENT) {
			return;
		}

		registry.registerIfAbsent(RetryProperties.class,
				context -> context.get(Binder.class)
					.bind(RetryProperties.PREFIX, RetryProperties.class)
					.orElseGet(RetryProperties::new));

		registry.registerIfAbsent(RetryTemplate.class, context -> {
			RetryProperties properties = context.get(RetryProperties.class);
			if (properties.isEnabled()) {
				return RetryTemplate.builder()
					.maxAttempts(properties.getMaxAttempts())
					.exponentialBackoff(properties.getInitialInterval(), properties.getMultiplier(),
							properties.getMaxInterval())
					.build();
			}
			return null;
		});
		registry.registerIfAbsent(LoaderInterceptor.class, context -> {
			RetryTemplate retryTemplate = context.get(RetryTemplate.class);
			if (retryTemplate != null) {
				return loadContext -> retryTemplate.execute(retryContext -> loadContext.getInvocation()
					.apply(loadContext.getLoaderContext(), loadContext.getResource()));
			}
			// disabled
			return null;
		});

	}

}
