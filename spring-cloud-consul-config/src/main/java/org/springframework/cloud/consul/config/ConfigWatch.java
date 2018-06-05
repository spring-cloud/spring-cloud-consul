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

package org.springframework.cloud.consul.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import io.micrometer.core.annotation.Timed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.style.ToStringCreator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * @author Spencer Gibb
 */
public class ConfigWatch implements ApplicationEventPublisherAware, SmartLifecycle {

	private static final Log log = LogFactory.getLog(ConfigWatch.class);

	private final ConsulConfigProperties properties;
	private final ConsulClient consul;
	private LinkedHashMap<String, Long> consulIndexes;
	private final TaskScheduler taskScheduler;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private ApplicationEventPublisher publisher;
	private boolean firstTime = true;
	private ScheduledFuture<?> watchFuture;

	public ConfigWatch(ConsulConfigProperties properties, ConsulClient consul, LinkedHashMap<String, Long> initialIndexes) {
		this(properties, consul, initialIndexes, getTaskScheduler());
    }

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		return taskScheduler;
	}

	public ConfigWatch(ConsulConfigProperties properties, ConsulClient consul, LinkedHashMap<String, Long> initialIndexes,
					   TaskScheduler taskScheduler) {
		this.properties = properties;
		this.consul = consul;
		this.consulIndexes = new LinkedHashMap<>(initialIndexes);
		this.taskScheduler = taskScheduler;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(this::watchConfigKeyValues,
					this.properties.getWatch().getDelay());
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

	@Timed(value ="consul.watch-config-keys")
	public void watchConfigKeyValues() {
		if (this.running.get()) {
			for (String context : this.consulIndexes.keySet()) {

				// turn the context into a Consul folder path (unless our config format are FILES)
				if (properties.getFormat() != FILES && !context.endsWith("/")) {
					context = context + "/";
				}

				try {
					Long currentIndex = this.consulIndexes.get(context);
					if (currentIndex == null) {
						currentIndex = -1L;
					}

					log.trace("watching consul for context '"+context+"' with index "+ currentIndex);

					// use the consul ACL token if found
					String aclToken = properties.getAclToken();
					if (StringUtils.isEmpty(aclToken)) {
					    aclToken = null;
					}

					Response<List<GetValue>> response = this.consul.getKVValues(context, aclToken,
							new QueryParams(this.properties.getWatch().getWaitTime(),
									currentIndex));

					// if response.value == null, response was a 404, otherwise it was a 200
					// reducing churn if there wasn't anything
					if (response.getValue() != null && !response.getValue().isEmpty()) {
						Long newIndex = response.getConsulIndex();

						if (newIndex != null && !newIndex.equals(currentIndex)) {
							// don't publish the same index again, don't publish the first time (-1) so index can be primed
							if (!this.consulIndexes.containsValue(newIndex) && !currentIndex.equals(-1L)) {
								log.trace("Context "+context + " has new index " + newIndex);
								RefreshEventData data = new RefreshEventData(context, currentIndex, newIndex);
								this.publisher.publishEvent(new RefreshEvent(this, data, data.toString()));
							} else if (log.isTraceEnabled()) {
								log.trace("Event for index already published for context "+context);
							}
							this.consulIndexes.put(context, newIndex);
						} else if (log.isTraceEnabled()) {
							log.trace("Same index for context "+context);
						}
					} else if (log.isTraceEnabled()) {
						log.trace("No value for context "+context);
					}

				} catch (Exception e) {
					// only fail fast on the initial query, otherwise just log the error
					if (firstTime && this.properties.isFailFast()) {
						log.error("Fail fast is set and there was an error reading configuration from consul.");
						ReflectionUtils.rethrowRuntimeException(e);
					} else if (log.isTraceEnabled()) {
						log.trace("Error querying consul Key/Values for context '" + context + "'", e);
					} else if (log.isWarnEnabled()) {
						// simplified one line log message in the event of an agent failure
						log.warn("Error querying consul Key/Values for context '" + context + "'. Message: " + e.getMessage());
					}
				}
			}
		}
		firstTime = false;
	}

	static class RefreshEventData {
		private final String context;
		private final Long prevIndex;
		private final Long newIndex;

		public RefreshEventData(String context, Long prevIndex, Long newIndex) {
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
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RefreshEventData that = (RefreshEventData) o;
			return Objects.equals(context, that.context) &&
					Objects.equals(prevIndex, that.prevIndex) &&
					Objects.equals(newIndex, that.newIndex);
		}

		@Override
		public int hashCode() {
			return Objects.hash(context, prevIndex, newIndex);
		}

		@Override
		public String toString() {
			return new ToStringCreator(this)
					.append("context", context)
					.append("prevIndex", prevIndex)
					.append("newIndex", newIndex)
					.toString();
		}
	}
}
