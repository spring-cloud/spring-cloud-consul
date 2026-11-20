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

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.commons.util.TaskSchedulerWrapper;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the catalog watch task scheduler of
 * {@link ConsulCatalogWatchAutoConfiguration}.
 */
class ConsulCatalogWatchAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ConsulCatalogWatchAutoConfiguration.class))
		.withBean(ConsulClient.class, ConsulCatalogWatchAutoConfigurationTests::consulClient)
		.withBean(ConsulDiscoveryProperties.class,
				() -> new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties())));

	@Test
	void catalogWatchTaskSchedulerIsNotExposedAsTaskScheduler() {
		this.contextRunner.run(context -> {
			assertThat(context).hasNotFailed()
				.hasSingleBean(ConsulCatalogWatch.class)
				.doesNotHaveBean(TaskScheduler.class);
			TaskSchedulerWrapper<?> wrapper = context.getBean(
					ConsulCatalogWatchAutoConfiguration.CATALOG_WATCH_TASK_SCHEDULER_NAME, TaskSchedulerWrapper.class);
			assertThat(wrapper.getTaskScheduler()).isInstanceOfSatisfying(ThreadPoolTaskScheduler.class,
					scheduler -> assertThat(scheduler.getThreadNamePrefix())
						.isEqualTo(ConsulCatalogWatchAutoConfiguration.CATALOG_WATCH_TASK_SCHEDULER_NAME + "-"));
			assertThat(catalogWatchTaskScheduler(context)).isSameAs(wrapper.getTaskScheduler());
		});
	}

	@Test
	void catalogWatchTaskSchedulerIsShutDownWithContext() {
		AtomicReference<ThreadPoolTaskScheduler> taskScheduler = new AtomicReference<>();
		this.contextRunner.run(context -> {
			assertThat(context).hasNotFailed();
			ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) context
				.getBean(ConsulCatalogWatchAutoConfiguration.CATALOG_WATCH_TASK_SCHEDULER_NAME,
						TaskSchedulerWrapper.class)
				.getTaskScheduler();
			assertThat(scheduler.getScheduledExecutor().isShutdown()).isFalse();
			taskScheduler.set(scheduler);
		});
		assertThat(taskScheduler.get().getScheduledExecutor().isShutdown()).isTrue();
	}

	@Test
	void applicationTaskSchedulerIsUsedForScheduledMethods() {
		this.contextRunner.withUserConfiguration(ApplicationSchedulingConfiguration.class).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context.getBean(TaskScheduler.class)).isSameAs(context.getBean("applicationScheduler"));
			String threadName = context.getBean(ApplicationSchedulingConfiguration.class).threadName.get(10,
					TimeUnit.SECONDS);
			assertThat(threadName).startsWith("application-scheduler-");
			assertThat(catalogWatchTaskScheduler(context)).isNotSameAs(context.getBean(TaskScheduler.class));
		});
	}

	@Test
	void springBootTaskSchedulerIsNotReplaced() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(TaskSchedulingAutoConfiguration.class))
			.withUserConfiguration(EnableSchedulingConfiguration.class)
			.run(context -> {
				assertThat(context).hasNotFailed().hasSingleBean(TaskScheduler.class).hasBean("taskScheduler");
				assertThat(catalogWatchTaskScheduler(context)).isNotSameAs(context.getBean(TaskScheduler.class));
			});
	}

	@Test
	void customCatalogWatchTaskSchedulerWrapperIsUsed() {
		ThreadPoolTaskScheduler customScheduler = new ThreadPoolTaskScheduler();
		this.contextRunner
			.withBean(ConsulCatalogWatchAutoConfiguration.CATALOG_WATCH_TASK_SCHEDULER_NAME, TaskSchedulerWrapper.class,
					() -> new TaskSchedulerWrapper<>(customScheduler))
			.run(context -> {
				assertThat(context).hasNotFailed().hasSingleBean(TaskSchedulerWrapper.class);
				assertThat(catalogWatchTaskScheduler(context)).isSameAs(customScheduler);
			});
	}

	@Test
	void customCatalogWatchTaskSchedulerBeanIsUsed() {
		ThreadPoolTaskScheduler customScheduler = new ThreadPoolTaskScheduler();
		this.contextRunner
			.withBean(ConsulCatalogWatchAutoConfiguration.CATALOG_WATCH_TASK_SCHEDULER_NAME, TaskScheduler.class,
					() -> customScheduler)
			.run(context -> {
				assertThat(context).hasNotFailed().doesNotHaveBean(TaskSchedulerWrapper.class);
				assertThat(catalogWatchTaskScheduler(context)).isSameAs(customScheduler);
			});
	}

	private static ConsulClient consulClient() {
		ConsulClient consulClient = mock(ConsulClient.class);
		when(consulClient.getCatalogServices(any(), any())).thenReturn(ResponseEntity.ok(Collections.emptyMap()));
		return consulClient;
	}

	private static Object catalogWatchTaskScheduler(AssertableApplicationContext context) {
		return ReflectionTestUtils.getField(context.getBean(ConsulCatalogWatch.class), "taskScheduler");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableScheduling
	static class EnableSchedulingConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	@EnableScheduling
	static class ApplicationSchedulingConfiguration {

		final CompletableFuture<String> threadName = new CompletableFuture<>();

		@Bean
		ThreadPoolTaskScheduler applicationScheduler() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			scheduler.setThreadNamePrefix("application-scheduler-");
			return scheduler;
		}

		@Scheduled(fixedDelay = 100)
		void recordThreadName() {
			this.threadName.complete(Thread.currentThread().getName());
		}

	}

}
