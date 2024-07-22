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

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServerConfigDataLocationResolver;
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
			if (!isDiscoveryEnabled(context)) {
				return null;
			}
			ConfigServerConfigDataLocationResolver.PropertyResolver propertyResolver = getPropertyResolver(context);
			return propertyResolver.resolveConfigurationProperties(ConsulProperties.PREFIX, ConsulProperties.class,
					ConsulProperties::new);
		});
		registry.registerIfAbsent(ConsulClient.class, context -> {
			if (!isDiscoveryEnabled(context)) {
				return null;
			}
			ConsulProperties consulProperties = context.get(ConsulProperties.class);
			return ConsulAutoConfiguration.createConsulClient(consulProperties,
					ConsulAutoConfiguration.createConsulRawClientBuilder());
		});
		registry.registerIfAbsent(ConsulDiscoveryClient.class, context -> {
			if (!isDiscoveryEnabled(context)) {
				return null;
			}
			ConfigServerConfigDataLocationResolver.PropertyResolver propertyResolver = getPropertyResolver(context);
			ConsulClient consulClient = context.get(ConsulClient.class);
			ConsulDiscoveryProperties properties = propertyResolver.resolveConfigurationProperties(
					ConsulDiscoveryProperties.PREFIX, ConsulDiscoveryProperties.class,
					() -> new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties())));
			return new ConsulDiscoveryClient(consulClient, properties);
		});
		// promote discovery client if created
		registry.addCloseListener(event -> {
			if (!isDiscoveryEnabled(event.getBootstrapContext())) {
				return;
			}
			ConsulDiscoveryClient discoveryClient = event.getBootstrapContext().get(ConsulDiscoveryClient.class);
			if (discoveryClient != null) {
				event.getApplicationContext()
					.getBeanFactory()
					.registerSingleton("consulDiscoveryClient", discoveryClient);
			}
		});

		registry.registerIfAbsent(ConfigServerInstanceProvider.Function.class, context -> {
			if (!isDiscoveryEnabled(context)) {
				return (id) -> Collections.emptyList();
			}
			ConsulDiscoveryClient discoveryClient = context.get(ConsulDiscoveryClient.class);
			return discoveryClient::getInstances;
		});
	}

	private static ConfigServerConfigDataLocationResolver.PropertyResolver getPropertyResolver(
			BootstrapContext context) {
		return context.getOrElseSupply(ConfigServerConfigDataLocationResolver.PropertyResolver.class,
				() -> new ConfigServerConfigDataLocationResolver.PropertyResolver(context.get(Binder.class),
						context.getOrElse(BindHandler.class, null)));
	}

	public static boolean isDiscoveryEnabled(BootstrapContext bootstrapContext) {
		ConfigServerConfigDataLocationResolver.PropertyResolver propertyResolver = getPropertyResolver(
				bootstrapContext);
		return propertyResolver.get(ConfigClientProperties.CONFIG_DISCOVERY_ENABLED, Boolean.class, false)
				&& propertyResolver.get(ConditionalOnConsulDiscoveryEnabled.PROPERTY, Boolean.class, true)
				&& propertyResolver.get("spring.cloud.discovery.enabled", Boolean.class, true);
	}

}
