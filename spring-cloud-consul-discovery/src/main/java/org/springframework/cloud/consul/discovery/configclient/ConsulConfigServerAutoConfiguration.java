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

package org.springframework.cloud.consul.discovery.configclient;

import javax.annotation.PostConstruct;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Extra configuration for config server if it happens to be registered with Consul.
 *
 * @author Dave Syer
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass({ ConsulDiscoveryProperties.class, ConsulClient.class, ConfigServerProperties.class })
public class ConsulConfigServerAutoConfiguration {

	/**
	 * The metadata key for the path for config server.
	 */
	public static final String CONFIG_PATH_KEY = "configPath";

	@Autowired(required = false)
	private ConsulDiscoveryProperties properties;

	@Autowired(required = false)
	private ConfigServerProperties server;

	@PostConstruct
	public void init() {
		if (this.properties == null || this.server == null) {
			return;
		}
		String prefix = this.server.getPrefix();
		if (StringUtils.hasText(prefix)) {
			this.properties.getMetadata().put(CONFIG_PATH_KEY, prefix);
		}
	}

}
