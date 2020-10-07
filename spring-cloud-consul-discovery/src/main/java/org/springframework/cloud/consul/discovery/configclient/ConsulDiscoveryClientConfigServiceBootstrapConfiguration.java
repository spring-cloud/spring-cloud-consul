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

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration;
import org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClientConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Helper for config client that wants to lookup the config server via discovery.
 *
 * @author Spencer Gibb
 * @author Tim Ysewyn
 */
@ConditionalOnClass(ConfigServicePropertySourceLocator.class)
@ConditionalOnProperty("spring.cloud.config.discovery.enabled")
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class,
		ConsulReactiveDiscoveryClientConfiguration.class })
public class ConsulDiscoveryClientConfigServiceBootstrapConfiguration {

}
