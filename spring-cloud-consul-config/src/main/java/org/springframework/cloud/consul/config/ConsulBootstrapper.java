/*
 * Copyright 2015-present the original author or authors.
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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.util.Assert;

public class ConsulBootstrapper implements BootstrapRegistryInitializer {

	private Function<BootstrapContext, ConsulClient> consulClientFactory;

	private LoaderInterceptor loaderInterceptor;

	static BootstrapRegistryInitializer fromConsulProperties(Function<ConsulProperties, ConsulClient> factory) {
		return registry -> registry.register(ConsulClient.class, context -> {
			ConsulProperties properties = context.get(ConsulProperties.class);
			return factory.apply(properties);
		});
	}

	static BootstrapRegistryInitializer fromBootstrapContext(Function<BootstrapContext, ConsulClient> factory) {
		return registry -> registry.register(ConsulClient.class, factory::apply);
	}

	static ConsulBootstrapper create() {
		return new ConsulBootstrapper();
	}

	// TODO: document there will be a ConsulProperties in BootstrapContext
	public ConsulBootstrapper withConsulClientFactory(Function<BootstrapContext, ConsulClient> consulClientFactory) {
		this.consulClientFactory = consulClientFactory;
		return this;
	}

	public ConsulBootstrapper withLoaderInterceptor(LoaderInterceptor loaderInterceptor) {
		this.loaderInterceptor = loaderInterceptor;
		return this;
	}

	@Override
	public void initialize(BootstrapRegistry registry) {
		if (consulClientFactory != null) {
			registry.register(ConsulClient.class, consulClientFactory::apply);
		}
		if (loaderInterceptor != null) {
			registry.register(LoaderInterceptor.class, BootstrapRegistry.InstanceSupplier.of(loaderInterceptor));
		}
	}

	public interface LoaderInterceptor extends Function<LoadContext, ConfigData> {

	}

	@FunctionalInterface
	public interface LoaderInvocation
			extends BiFunction<ConfigDataLoaderContext, ConsulConfigDataResource, ConfigData> {

	}

	public static class LoadContext {

		private final ConfigDataLoaderContext loaderContext;

		private final ConsulConfigDataResource resource;

		private final Binder binder;

		private final LoaderInvocation invocation;

		LoadContext(ConfigDataLoaderContext loaderContext, ConsulConfigDataResource resource, Binder binder,
				LoaderInvocation invocation) {
			Assert.notNull(loaderContext, "loaderContext may not be null");
			Assert.notNull(resource, "resource may not be null");
			Assert.notNull(binder, "binder may not be null");
			Assert.notNull(invocation, "invocation may not be null");
			this.loaderContext = loaderContext;
			this.resource = resource;
			this.binder = binder;
			this.invocation = invocation;
		}

		public ConfigDataLoaderContext getLoaderContext() {
			return this.loaderContext;
		}

		public ConsulConfigDataResource getResource() {
			return this.resource;
		}

		public Binder getBinder() {
			return this.binder;
		}

		public LoaderInvocation getInvocation() {
			return this.invocation;
		}

	}

}
