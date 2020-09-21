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

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.Bootstrapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;

public class ConsulConfigServerBootstrapper implements Bootstrapper {

	@Override
	public void intitialize(BootstrapRegistry registry) {
		registry.registerIfAbsent(ConsulProperties.class, context -> {
			Binder binder = context.get(Binder.class);
			return binder.bind(ConsulProperties.PREFIX, ConsulProperties.class).orElseGet(ConsulProperties::new);
		});
		registry.registerIfAbsent(ConsulClient.class, context -> {
			ConsulProperties consulProperties = context.get(ConsulProperties.class);
			return ConsulAutoConfiguration.createConsulClient(consulProperties);
		});
		registry.registerIfAbsent(ConfigServerInstanceProvider.Function.class, context -> {
			Binder binder = context.get(Binder.class);
			boolean enabled = binder.bind(ConfigClientProperties.CONFIG_DISCOVERY_ENABLED, Boolean.class).orElse(false);
			if (!enabled) {
				return null;
			}
			ConsulClient consulClient = context.get(ConsulClient.class);
			ConsulDiscoveryProperties properties = binder
					.bind(ConsulDiscoveryProperties.PREFIX, ConsulDiscoveryProperties.class)
					.orElseGet(() -> new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties())));
			return new ConsulDiscoveryClient(consulClient, properties)::getInstances;
		});

	}

}
