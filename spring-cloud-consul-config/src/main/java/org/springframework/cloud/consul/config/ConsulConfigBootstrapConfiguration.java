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

package org.springframework.cloud.consul.config;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 * @author Edvin Eriksson
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnConsulEnabled
public class ConsulConfigBootstrapConfiguration {

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties
	@Import(ConsulAutoConfiguration.class)
	@ConditionalOnProperty(name = "spring.cloud.consul.config.enabled", matchIfMissing = true)
	protected static class ConsulPropertySourceConfiguration {

		@Autowired
		private ConsulClient consul;

		@Bean
		@ConditionalOnMissingBean
		public ConsulConfigProperties consulConfigProperties(Environment env) {
			ConsulConfigProperties properties = new ConsulConfigProperties();
			if (StringUtils.isEmpty(properties.getName())) {
				properties.setName(env.getProperty("spring.application.name", "application"));
			}
			return properties;
		}

		@Bean
		public ConsulPropertySourceLocator consulPropertySourceLocator(ConsulConfigProperties consulConfigProperties) {
			return new ConsulPropertySourceLocator(this.consul, consulConfigProperties);
		}

	}

}
