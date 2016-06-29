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

package org.springframework.cloud.consul.binder.config;

/**
 */

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.binder.ConsulBinder;
import org.springframework.cloud.consul.binder.EventService;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configures the Consul binder.
 *
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({ PropertyPlaceholderAutoConfiguration.class })
@EnableConfigurationProperties({ConsulBinderProperties.class})
public class ConsulBinderConfiguration {

	@Autowired
	private ConsulBinderProperties consulBinderProperties;

	@Autowired(required = false)
	protected ObjectMapper objectMapper = new ObjectMapper();

	@Bean
	@ConditionalOnMissingBean
	public EventService eventService(ConsulClient consulClient) {
		return new EventService(consulBinderProperties, consulClient, objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulBinder consulClientBinder(EventService eventService) {
		return new ConsulBinder(eventService);
	}

	//TODO: create consul client if needed
}
