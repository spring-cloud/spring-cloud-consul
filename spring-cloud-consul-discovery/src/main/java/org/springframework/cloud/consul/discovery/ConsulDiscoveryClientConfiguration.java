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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
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

	@Bean
	@ConditionalOnMissingBean
	public ConsulLifecycle consulLifecycle(ConsulDiscoveryProperties discoveryProperties,
			HeartbeatProperties heartbeatProperties) {
		return new ConsulLifecycle(consulClient, discoveryProperties, heartbeatProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

	@Bean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	public ConsulDiscoveryProperties consulDiscoveryProperties(InetUtils inetUtils) {
		return new ConsulDiscoveryProperties(inetUtils);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulDiscoveryClient consulDiscoveryClient(ConsulLifecycle consulLifecycle,
			ServerProperties serverProperties,
			ConsulDiscoveryProperties discoveryProperties) {
		return new ConsulDiscoveryClient(consulClient, consulLifecycle,
				discoveryProperties, serverProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulCatalogWatch consulCatalogWatch(
			ConsulDiscoveryProperties discoveryProperties) {
		return new ConsulCatalogWatch(discoveryProperties, consulClient);
	}
}
