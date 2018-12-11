/*
 * Copyright 2013-2018 the original author or authors.
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author Spencer Gibb
 * @author Olga Maciaszek-Sharma
 */
@Configuration
@ConditionalOnConsulEnabled
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.enabled", matchIfMissing = true)
@EnableConfigurationProperties
@AutoConfigureBefore({ SimpleDiscoveryClientAutoConfiguration.class,
		CommonsClientAutoConfiguration.class })
public class ConsulDiscoveryClientConfiguration {

	public static final String CATALOG_WATCH_TASK_SCHEDULER_NAME = "catalogWatchTaskScheduler";

	@Autowired
	private ConsulClient consulClient;

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	//TODO: move to service-registry for Edgware
	public TtlScheduler ttlScheduler(HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

	@Bean
	//TODO: move to service-registry for Edgware
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	//TODO: Split appropriate values to service-registry for Edgware
	@ConditionalOnMissingBean
	public ConsulDiscoveryProperties consulDiscoveryProperties(InetUtils inetUtils) {
		return new ConsulDiscoveryProperties(inetUtils);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulDiscoveryClient consulDiscoveryClient(ConsulDiscoveryProperties discoveryProperties) {
		return new ConsulDiscoveryClient(consulClient, discoveryProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(name = "spring.cloud.consul.discovery.catalog-services-watch.enabled", matchIfMissing = true)
	public ConsulCatalogWatch consulCatalogWatch(
			ConsulDiscoveryProperties discoveryProperties,
			@Qualifier(CATALOG_WATCH_TASK_SCHEDULER_NAME) TaskScheduler taskScheduler) {
		return new ConsulCatalogWatch(discoveryProperties, consulClient, taskScheduler);
	}

	@Bean(name = CATALOG_WATCH_TASK_SCHEDULER_NAME)
	@ConditionalOnProperty(name = "spring.cloud.consul.discovery.catalog-services-watch.enabled", matchIfMissing = true)
	public TaskScheduler catalogWatchTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}
}
