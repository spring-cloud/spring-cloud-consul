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

package org.springframework.cloud.consul.discovery.configclient;

import java.util.Collections;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import org.apache.commons.logging.Log;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.discovery.ConditionalOnConsulDiscoveryEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.util.ClassUtils;

public class ConsulConfigServerBootstrapper implements BootstrapRegistryInitializer {

	@Override
	public void initialize(BootstrapRegistry registry) {
		if (!ClassUtils.isPresent("org.springframework.cloud.config.client.ConfigServerInstanceProvider", null) ||
		// don't run if bootstrap enabled, how to check the property?
				ClassUtils.isPresent("org.springframework.cloud.bootstrap.marker.Marker", null)) {
			return;
		}
		// create consul client
		registry.registerIfAbsent(ConsulProperties.class, context -> {
			Binder binder = context.get(Binder.class);
			if (!isDiscoveryEnabled(binder)) {
				return null;
			}
			return binder.bind(ConsulProperties.PREFIX, Bindable.of(ConsulProperties.class), getBindHandler(context))
					.orElseGet(ConsulProperties::new);
		});
		registry.registerIfAbsent(ConsulClient.class, context -> {
			if (!isDiscoveryEnabled(context.get(Binder.class))) {
				return null;
			}
			ConsulProperties consulProperties = context.get(ConsulProperties.class);
			return ConsulAutoConfiguration.createConsulClient(consulProperties);
		});
		registry.registerIfAbsent(ConsulDiscoveryClient.class, context -> {
			Binder binder = context.get(Binder.class);
			if (!isDiscoveryEnabled(binder)) {
				return null;
			}
			ConsulClient consulClient = context.get(ConsulClient.class);
			ConsulDiscoveryProperties properties = binder
					.bind(ConsulDiscoveryProperties.PREFIX, Bindable.of(ConsulDiscoveryProperties.class),
							getBindHandler(context))
					.orElseGet(() -> new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties())));
			return new ConsulDiscoveryClient(consulClient, properties);
		});
		// promote discovery client if created
		registry.addCloseListener(event -> {
			if (!isDiscoveryEnabled(event.getBootstrapContext().get(Binder.class))) {
				return;
			}
			ConsulDiscoveryClient discoveryClient = event.getBootstrapContext().get(ConsulDiscoveryClient.class);
			if (discoveryClient != null) {
				event.getApplicationContext().getBeanFactory().registerSingleton("consulDiscoveryClient",
						discoveryClient);
			}
		});

		// We need to pass the lambda here so we do not create a new instance of
		// ConfigServerInstanceProvider.Function
		// which would result in a ClassNotFoundException when Spring Cloud Config is not
		// on the classpath
		registry.registerIfAbsent(ConfigServerInstanceProvider.Function.class, ConsulFunction::create);
	}

	private BindHandler getBindHandler(org.springframework.boot.BootstrapContext context) {
		return context.getOrElse(BindHandler.class, null);
	}

	private static boolean isDiscoveryEnabled(Binder binder) {
		return binder.bind(ConfigClientProperties.CONFIG_DISCOVERY_ENABLED, Boolean.class).orElse(false)
				&& binder.bind(ConditionalOnConsulDiscoveryEnabled.PROPERTY, Boolean.class).orElse(true)
				&& binder.bind("spring.cloud.discovery.enabled", Boolean.class).orElse(true);
	}

	/*
	 * This Function is executed when loading config data. Because of this we cannot rely
	 * on the BootstrapContext because Boot has not finished loading all the configuration
	 * data so if we ask the BootstrapContext for configuration data it will not have it.
	 * The apply method in this function is passed the Binder and BindHandler from the
	 * config data context which has the configuration properties that have been loaded so
	 * far in the config data process.
	 *
	 * We will create many of the same beans in this function as we do above in the
	 * initializer above. We do both to maintain compatibility since we are promoting
	 * those beans to the main application context.
	 */
	static final class ConsulFunction implements ConfigServerInstanceProvider.Function {

		private final BootstrapContext context;

		private ConsulFunction(BootstrapContext context) {
			this.context = context;
		}

		public static ConsulFunction create(BootstrapContext context) {
			return new ConsulFunction(context);
		}

		@Override
		public List<ServiceInstance> apply(String serviceId) {
			return apply(serviceId, null, null, null);
		}

		@Override
		public List<ServiceInstance> apply(String serviceId, Binder binder, BindHandler bindHandler, Log log) {
			if (binder == null || !isDiscoveryEnabled(binder)) {
				return Collections.emptyList();
			}

			ConsulProperties consulProperties = binder
					.bind(ConsulProperties.PREFIX, Bindable.of(ConsulProperties.class), bindHandler)
					.orElseGet(ConsulProperties::new);
			ConsulClient consulClient = ConsulAutoConfiguration.createConsulClient(consulProperties);
			ConsulDiscoveryProperties properties = binder
					.bind(ConsulDiscoveryProperties.PREFIX, Bindable.of(ConsulDiscoveryProperties.class), bindHandler)
					.orElseGet(() -> new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties())));
			ConsulDiscoveryClient discoveryClient = new ConsulDiscoveryClient(consulClient, properties);

			return discoveryClient.getInstances(serviceId);
		}

	}

}
