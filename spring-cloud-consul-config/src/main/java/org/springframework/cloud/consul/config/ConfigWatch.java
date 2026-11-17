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

package org.springframework.cloud.consul.config;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import io.micrometer.core.annotation.Timed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.ConsulHeaders;
import org.springframework.cloud.consul.model.http.kv.GetValue;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * @author Spencer Gibb
 */
public class ConfigWatch implements ApplicationEventPublisherAware, SmartLifecycle {

	private static final Log log = LogFactory.getLog(ConfigWatch.class);

	private final ConsulConfigProperties properties;

	private final ConsulClient consul;

	private final TaskScheduler taskScheduler;

	private final Set<String> populatedContexts;

	private final AtomicBoolean running = new AtomicBoolean(false);

	private LinkedHashMap<String, Long> consulIndexes;

	private ApplicationEventPublisher publisher;

	private boolean firstTime = true;

	private volatile ScheduledFuture<?> watchFuture;

	public ConfigWatch(ConsulConfigProperties properties, ConsulClient consul,
			LinkedHashMap<String, Long> initialIndexes) {
		this(properties, consul, initialIndexes, getTaskScheduler());
	}

	public ConfigWatch(ConsulConfigProperties properties, ConsulClient consul,
			LinkedHashMap<String, Long> initialIndexes, TaskScheduler taskScheduler) {
		this(properties, consul, initialIndexes, Collections.emptySet(), taskScheduler);
	}

	public ConfigWatch(ConsulConfigProperties properties, ConsulClient consul,
			LinkedHashMap<String, Long> initialIndexes, Set<String> populatedContexts, TaskScheduler taskScheduler) {
		this.properties = properties;
		this.consul = consul;
		this.consulIndexes = new LinkedHashMap<>();
		initialIndexes
			.forEach((context, index) -> this.consulIndexes.put(normalizeContext(context, properties), index));

		this.populatedContexts = new HashSet<>();
		populatedContexts.forEach(context -> this.populatedContexts.add(normalizeContext(context, properties)));
		this.taskScheduler = taskScheduler;
	}

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		return taskScheduler;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(this::watchConfigKeyValues,
					Duration.ofMillis(this.properties.getWatch().getDelay()));
		}
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
	public int getPhase() {
		return 0;
	}

	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
			this.watchFuture.cancel(true);
		}
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	@Timed("consul.watch-config-keys")
	public void watchConfigKeyValues() {
		if (!this.running.get()) {
			return;
		}

		for (String context : this.consulIndexes.keySet()) {

			// Stop processing when the watcher is stopped. No further polling is
			// required.
			if (!this.running.get()) {
				return;
			}

			// Turn the context into a Consul folder path unless the config format
			// is FILES.
			context = normalizeContext(context, this.properties);

			try {
				Long currentIndex = this.consulIndexes.get(context);
				if (currentIndex == null) {
					currentIndex = -1L;
				}

				if (log.isTraceEnabled()) {
					log.trace("watching consul for context '" + context + "' with index " + currentIndex);
				}

				// Use the Consul ACL token if found.
				String aclToken = this.properties.getAclToken();
				if (ObjectUtils.isEmpty(aclToken)) {
					aclToken = null;
				}

				ResponseEntity<List<GetValue>> response = this.consul.getKVValues(context, aclToken,
						(long) this.properties.getWatch().getWaitTime(), currentIndex);

				if (shouldProcessResponse(response)) {
					Long newIndex = ConsulHeaders.getConsulIndex(response);

					if (newIndex != null && !newIndex.equals(currentIndex)) {
						boolean hasValues = response.hasBody() && !response.getBody().isEmpty();

						if (hasValues) {
							this.populatedContexts.add(context);
						}

						boolean deletionResponse = this.properties.getWatch().isRefreshOnDelete() && !hasValues;
						boolean contextWasPopulated = this.populatedContexts.contains(context);

						// Don't publish the same index again, don't publish the first
						// time (-1) so the index can be primed.
						boolean indexAlreadyPublished = this.consulIndexes.containsValue(newIndex);

						if (shouldPublishRefreshEvent(indexAlreadyPublished, deletionResponse, contextWasPopulated,
								currentIndex)) {
							if (log.isTraceEnabled()) {
								log.trace("Context " + context + " has new index " + newIndex);
							}

							RefreshEventData data = new RefreshEventData(context, currentIndex, newIndex);

							// Skip event publication if the watcher has been stopped.
							if (!this.running.get()) {
								return;
							}

							this.publisher.publishEvent(new RefreshEvent(this, data, data.toString()));

							if (deletionResponse) {
								this.populatedContexts.remove(context);
							}
						}
						else if (log.isTraceEnabled()) {
							log.trace("Event for index already published or context was never populated: " + context);
						}

						this.consulIndexes.put(context, newIndex);
					}
					else if (log.isTraceEnabled()) {
						log.trace("Same index for context " + context);
					}
				}
				else if (log.isTraceEnabled()) {
					log.trace("No value for context " + context);
				}

			}
			catch (Exception e) {
				if (!this.running.get() && (isInterruptedException(e) || Thread.currentThread().isInterrupted())) {
					// The scheduled task was interrupted during shutdown.
					return;
				}

				// Only fail fast on the initial query, otherwise just log the error.
				if (this.firstTime && this.properties.isFailFast()) {
					log.error("Fail fast is set and there was an error reading configuration from consul.");
					ReflectionUtils.rethrowRuntimeException(e);
				}
				else if (log.isTraceEnabled()) {
					log.trace("Error querying consul Key/Values for context '" + context + "'", e);
				}
				else if (log.isWarnEnabled()) {
					// Simplified one line log message in the event of an agent
					// failure.
					log.warn("Error querying consul Key/Values for context '" + context + "'. Message: "
							+ e.getMessage());
				}
			}
		}

		this.firstTime = false;
	}

	/**
	 * Determines whether the Consul response should be processed. Empty or not-found
	 * responses are processed only when refresh-on-delete is enabled.
	 * @param response the Consul response
	 * @return whether the response should be processed
	 */
	private boolean shouldProcessResponse(ResponseEntity<List<GetValue>> response) {
		if (HttpStatus.OK.isSameCodeAs(response.getStatusCode()) && response.hasBody()) {
			return !response.getBody().isEmpty() || this.properties.getWatch().isRefreshOnDelete();
		}

		return HttpStatus.NOT_FOUND.isSameCodeAs(response.getStatusCode())
				&& this.properties.getWatch().isRefreshOnDelete();
	}

	private boolean isInterruptedException(Throwable exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof InterruptedException) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}

	static String normalizeContext(String context, ConsulConfigProperties properties) {
		if (properties.getFormat() != FILES) {
			if (context.startsWith("/")) {
				context = context.substring(1);
			}

			if (!context.endsWith("/")) {
				context = context + "/";
			}
		}

		return context;
	}

	private boolean shouldPublishRefreshEvent(boolean indexAlreadyPublished, boolean deletionResponse,
			boolean contextWasPopulated, Long currentIndex) {

		return !currentIndex.equals(-1L) && (!indexAlreadyPublished || (deletionResponse && contextWasPopulated))
				&& (!deletionResponse || contextWasPopulated);
	}

	public static class RefreshEventData {

		private final String context;

		private final Long prevIndex;

		private final Long newIndex;

		RefreshEventData(String context, Long prevIndex, Long newIndex) {
			this.context = context;
			this.prevIndex = prevIndex;
			this.newIndex = newIndex;
		}

		public String getContext() {
			return this.context;
		}

		public Long getPrevIndex() {
			return this.prevIndex;
		}

		public Long getNewIndex() {
			return this.newIndex;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			RefreshEventData that = (RefreshEventData) o;
			return Objects.equals(this.context, that.context) && Objects.equals(this.prevIndex, that.prevIndex)
					&& Objects.equals(this.newIndex, that.newIndex);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.context, this.prevIndex, this.newIndex);
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("context", this.context)
				.append("prevIndex", this.prevIndex)
				.append("newIndex", this.newIndex)
				.toString();
		}

	}

}
