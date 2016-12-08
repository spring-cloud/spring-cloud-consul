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

package org.springframework.cloud.consul.discovery;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnConsulEnabled
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class ConsulDiscoveryClientConfiguration {

	@Autowired
	private ConsulClient consulClient;

	@Autowired(required = false)
	private ServerProperties serverProperties;

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	//TODO: move to service-registry for Edgware
	public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

	@Bean
	//TODO: move to service-registry for Edgware
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	//TODO: Split appropriate values to service-registry for Edgware
	public ConsulDiscoveryProperties consulDiscoveryProperties(InetUtils inetUtils) {
		return new ConsulDiscoveryProperties(inetUtils);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulDiscoveryClient consulDiscoveryClient(ConsulDiscoveryProperties discoveryProperties, final ApplicationContext context) {
		ConsulDiscoveryClient discoveryClient = new ConsulDiscoveryClient(consulClient,
				discoveryProperties, new LifecycleRegistrationResolver(context));
		discoveryClient.setServerProperties(serverProperties); //null ok
		return discoveryClient;
	}

	class LifecycleRegistrationResolver implements ConsulDiscoveryClient.LocalResolver {
		private ApplicationContext context;

		public LifecycleRegistrationResolver(ApplicationContext context) {
			this.context = context;
		}

		@Override
		public String getServiceId() {
			ConsulRegistration registration = getBean(ConsulRegistration.class);
			if (registration != null) {
				return registration.getServiceId();
			}
			ConsulLifecycle lifecycle = getBean(ConsulLifecycle.class);
			if (lifecycle != null) {
				return lifecycle.getServiceId();
			}
			throw new IllegalStateException("Must have one of ConsulRegistration or ConsulLifecycle");
		}

		@Override
		public Integer getPort() {
			ConsulRegistration registration = getBean(ConsulRegistration.class);
			if (registration != null) {
				return registration.getService().getPort();
			}
			ConsulLifecycle lifecycle = getBean(ConsulLifecycle.class);
			if (lifecycle != null) {
				return lifecycle.getConfiguredPort();
			}
			throw new IllegalStateException("Must have one of ConsulRegistration or ConsulLifecycle");
		}

		<T> T getBean(Class<T> type) {
			try {
				return context.getBean(type);
			} catch (NoSuchBeanDefinitionException e) {
			}
			return null;
		}
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "spring.cloud.consul.discovery.catalogServicesWatch.enabled", matchIfMissing = true)
	public ConsulCatalogWatch consulCatalogWatch(
			ConsulDiscoveryProperties discoveryProperties) {
		return new ConsulCatalogWatch(discoveryProperties, consulClient);
	}
}
