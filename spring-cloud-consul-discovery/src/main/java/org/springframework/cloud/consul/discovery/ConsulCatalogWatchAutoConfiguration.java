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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Auto configuration for the catalog watcher.
 *
 * @author Tim Ysewyn
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnConsulEnabled
@ConditionalOnProperty(
		value = "spring.cloud.consul.discovery.catalog-services-watch.enabled",
		matchIfMissing = true)
@ConditionalOnDiscoveryEnabled
@AutoConfigureAfter({ ConsulDiscoveryClientConfiguration.class })
@ConditionalOnBean(ConsulDiscoveryProperties.class)
public class ConsulCatalogWatchAutoConfiguration {

	/**
	 * Name of the catalog watch task scheduler bean.
	 */
	public static final String CATALOG_WATCH_TASK_SCHEDULER_NAME = "catalogWatchTaskScheduler";

	@Bean
	@ConditionalOnMissingBean
	public ConsulCatalogWatch consulCatalogWatch(
			ConsulDiscoveryProperties discoveryProperties, ConsulClient consulClient,
			@Qualifier(CATALOG_WATCH_TASK_SCHEDULER_NAME) TaskScheduler taskScheduler) {
		return new ConsulCatalogWatch(discoveryProperties, consulClient, taskScheduler);
	}

	@Bean(name = CATALOG_WATCH_TASK_SCHEDULER_NAME)
	public TaskScheduler catalogWatchTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

}
