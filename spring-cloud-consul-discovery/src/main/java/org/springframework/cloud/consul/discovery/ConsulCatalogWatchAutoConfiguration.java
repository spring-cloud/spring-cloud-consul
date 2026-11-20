/*
 * Copyright 2013-present the original author or authors.
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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.commons.util.TaskSchedulerWrapper;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.ConsulClient;
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
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.catalog-services-watch.enabled", matchIfMissing = true)
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
	public ConsulCatalogWatch consulCatalogWatch(ConsulDiscoveryProperties discoveryProperties,
			ConsulClient consulClient,
			@Qualifier(CATALOG_WATCH_TASK_SCHEDULER_NAME) ObjectProvider<TaskSchedulerWrapper<? extends TaskScheduler>> taskSchedulerWrapper,
			@Qualifier(CATALOG_WATCH_TASK_SCHEDULER_NAME) ObjectProvider<TaskScheduler> taskScheduler) {
		// a plain TaskScheduler bean with the same name is still honoured for backwards
		// compatibility
		TaskScheduler scheduler = taskScheduler
			.getIfAvailable(() -> taskSchedulerWrapper.getObject().getTaskScheduler());
		return new ConsulCatalogWatch(discoveryProperties, consulClient, scheduler);
	}

	/**
	 * The {@link TaskScheduler} used by the {@link ConsulCatalogWatch}. It is wrapped so
	 * that it is not exposed as a {@link TaskScheduler} bean and does not replace the
	 * application's own scheduler or the one auto-configured by Spring Boot.
	 * @return the wrapped catalog watch task scheduler
	 */
	@Bean(name = CATALOG_WATCH_TASK_SCHEDULER_NAME)
	@ConditionalOnMissingBean(name = CATALOG_WATCH_TASK_SCHEDULER_NAME)
	public TaskSchedulerWrapper<TaskScheduler> catalogWatchTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setThreadNamePrefix(CATALOG_WATCH_TASK_SCHEDULER_NAME + "-");
		return new TaskSchedulerWrapper<>(taskScheduler);
	}

}
