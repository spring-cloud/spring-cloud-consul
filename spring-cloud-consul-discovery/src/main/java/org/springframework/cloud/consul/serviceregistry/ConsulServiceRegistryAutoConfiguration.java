/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnConsulEnabled
@ConditionalOnProperty(value = "spring.cloud.service-registry.enabled", matchIfMissing = true)
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
public class ConsulServiceRegistryAutoConfiguration {

	@Autowired(required = false)
	private TtlScheduler ttlScheduler;

	@Bean
	@ConditionalOnMissingBean
	public ConsulServiceRegistry consulServiceRegistry(ConsulClient consulClient, ConsulDiscoveryProperties properties,
													   HeartbeatProperties heartbeatProperties) {
		return new ConsulServiceRegistry(consulClient, properties, ttlScheduler, heartbeatProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	public TtlScheduler ttlScheduler(ConsulClient consulClient, HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

	@Bean
	@ConditionalOnMissingBean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulDiscoveryProperties consulDiscoveryProperties(InetUtils inetUtils) {
		return new ConsulDiscoveryProperties(inetUtils);
	}

}
