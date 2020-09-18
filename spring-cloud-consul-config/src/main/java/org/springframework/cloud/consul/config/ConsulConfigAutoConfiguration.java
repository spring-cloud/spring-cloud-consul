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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author Spencer Gibb
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnConsulEnabled
@ConditionalOnProperty(name = "spring.cloud.consul.config.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class ConsulConfigAutoConfiguration {

	/**
	 * Name of the config watch task scheduler bean.
	 */
	public static final String CONFIG_WATCH_TASK_SCHEDULER_NAME = "configWatchTaskScheduler";

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(RefreshEndpoint.class)
	@ConditionalOnProperty(name = "spring.cloud.consul.config.watch.enabled", matchIfMissing = true)
	protected static class ConsulRefreshConfiguration {

		@Bean
		@ConditionalOnBean(ConsulConfigIndexes.class)
		public ConfigWatch configWatch(ConsulConfigProperties properties, ConsulConfigIndexes indexes,
				ConsulClient consul, @Qualifier(CONFIG_WATCH_TASK_SCHEDULER_NAME) TaskScheduler taskScheduler) {
			return new ConfigWatch(properties, consul, indexes.getIndexes(), taskScheduler);
		}

		@Bean(name = CONFIG_WATCH_TASK_SCHEDULER_NAME)
		public TaskScheduler configWatchTaskScheduler() {
			return new ThreadPoolTaskScheduler();
		}

	}

}
