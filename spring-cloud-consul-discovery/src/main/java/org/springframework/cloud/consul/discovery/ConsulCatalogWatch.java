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

package org.springframework.cloud.consul.discovery;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import io.micrometer.core.annotation.Timed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author Spencer Gibb
 */
public class ConsulCatalogWatch implements ApplicationEventPublisherAware, SmartLifecycle {
	private static final Log log = LogFactory.getLog(ConsulDiscoveryClient.class);

	private final ConsulDiscoveryProperties properties;
	private final ConsulClient consul;
	private final TaskScheduler taskScheduler;
	private final AtomicReference<BigInteger> catalogServicesIndex = new AtomicReference<>();
	private final AtomicBoolean running = new AtomicBoolean(false);
	private ApplicationEventPublisher publisher;
	private ScheduledFuture<?> watchFuture;

	public ConsulCatalogWatch(ConsulDiscoveryProperties properties, ConsulClient consul) {
		this(properties, consul, getTaskScheduler());
    }

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		return taskScheduler;
	}

	public ConsulCatalogWatch(ConsulDiscoveryProperties properties, ConsulClient consul, TaskScheduler taskScheduler) {
		this.properties = properties;
		this.consul = consul;
		this.taskScheduler = taskScheduler;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		this.stop();
		callback.run();
	}

	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(this::catalogServicesWatch,
					this.properties.getCatalogServicesWatchDelay());
		}
	}

	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
			this.watchFuture.cancel(true);
		}
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Timed(value ="consul.watch-catalog-services")
	public void catalogServicesWatch() {
		try {
			long index = -1;
			if (catalogServicesIndex.get() != null) {
				index = catalogServicesIndex.get().longValue();
			}

			Response<Map<String, List<String>>> response = consul
					.getCatalogServices(new QueryParams(properties
							.getCatalogServicesWatchTimeout(), index), properties.getAclToken());
			Long consulIndex = response.getConsulIndex();
			if (consulIndex != null) {
				catalogServicesIndex.set(BigInteger.valueOf(consulIndex));
			}

			if (log.isTraceEnabled()) {
				log.trace("Received services update from consul: "+response.getValue()
								+", index: "+ consulIndex);
			}
			publisher.publishEvent(new HeartbeatEvent(this, consulIndex));
		}
		catch (Exception e) {
			log.error("Error watching Consul CatalogServices", e);
		}
	}
}
