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

package org.springframework.cloud.consul;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(name = "spring.cloud.consul.enabled", matchIfMissing = true)
public class ConsulAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ConsulProperties consulProperties() {
		return new ConsulProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulClient consulClient() {
		return new ConsulClient(consulProperties().getHost(), consulProperties()
				.getPort());
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulEndpoint consulEndpoint() {
		return new ConsulEndpoint();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulHealthIndicator consulHealthIndicator() {
		return new ConsulHealthIndicator();
	}
}
