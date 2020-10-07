/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.consul.support;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for the heartbeat.
 *
 * @author Tim Ysewyn
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnConsulEnabled
@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
@ConditionalOnDiscoveryEnabled
@AutoConfigureBefore({ ConsulServiceRegistryAutoConfiguration.class })
@AutoConfigureAfter({ ConsulDiscoveryClientConfiguration.class })
public class ConsulHeartbeatAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties, ConsulClient consulClient) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

}
