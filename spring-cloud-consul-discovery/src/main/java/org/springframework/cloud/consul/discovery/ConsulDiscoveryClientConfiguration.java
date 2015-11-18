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

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	public ConsulLifecycle consulLifecycle() {
		return new ConsulLifecycle(consulClient, lifecycleProperties(), consulDiscoveryProperties(), heartbeatProperties());
	}

	@Bean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	public TtlScheduler ttlScheduler() {
		return new TtlScheduler(heartbeatProperties(), consulClient);
	}

	@Bean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	public LifecycleProperties lifecycleProperties() {
		return new LifecycleProperties();
	}

	@Bean
	public ConsulDiscoveryProperties consulDiscoveryProperties() {
		return new ConsulDiscoveryProperties();
	}

	@Bean
	public ConsulDiscoveryClient consulDiscoveryClient(ServerProperties serverProperties) {
		return new ConsulDiscoveryClient(consulClient, consulLifecycle(), consulDiscoveryProperties(), serverProperties);
	}

	@Bean
	public ConsulCatalogWatch consulCatalogWatch() {
		return new ConsulCatalogWatch(consulDiscoveryProperties(), consulClient);
	}
}
