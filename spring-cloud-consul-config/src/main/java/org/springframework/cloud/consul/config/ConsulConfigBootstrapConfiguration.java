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

package org.springframework.cloud.consul.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Configuration
@Import(ConsulAutoConfiguration.class)
@EnableConfigurationProperties
@ConditionalOnProperty(name = "spring.cloud.consul.config.enabled", matchIfMissing = true)
public class ConsulConfigBootstrapConfiguration {

	@Autowired
	private ConsulClient consul;

	@Bean
	public ConsulConfigProperties consulConfigProperties() {
		return new ConsulConfigProperties();
	}

	@Bean
	public ConsulPropertySourceLocator consulPropertySourceLocator() {
		return new ConsulPropertySourceLocator(consul, consulConfigProperties());
	}
	
	@Bean
	@ConditionalOnProperty(name="spring.cloud.consul.config.watch", matchIfMissing = false)
	public ConsulConfigWatch consulConfigWatch() {
	    return new ConsulConfigWatch(consulConfigProperties());
	}
}
