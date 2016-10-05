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

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.util.ReflectionUtils;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ConfigWatch implements Closeable, ApplicationEventPublisherAware {

	private final ConsulConfigProperties properties;
	private final List<String> contexts;
	private final ConsulClient consul;
	private AtomicBoolean running = new AtomicBoolean(false);
	private ApplicationEventPublisher publisher;
	private HashMap<String, Long> consulIndexes = new HashMap<>();
	private Boolean initialized = false;

	public ConfigWatch(ConsulConfigProperties properties, List<String> contexts, ConsulClient consul) {
		this.properties = properties;
		this.contexts = contexts;
		this.consul = consul;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@PostConstruct
	public void start() {
		this.running.compareAndSet(false, true);
	}

	@Scheduled(fixedDelayString = "${spring.cloud.consul.config.watch.delay:1000}")
	public void watchConfigKeyValues() {
		if (this.running.get()) {
			for (String context : this.contexts) {

				// turn the context into a Consul folder path (unless our config format are FILES)
				if (properties.getFormat() != FILES && !context.endsWith("/")) {
					context = context + "/";
				}

				try {
					Long currentIndex = this.consulIndexes.get(context);
					if (currentIndex == null) {
						currentIndex = -1L;
					}

					Response<List<GetValue>> response = this.consul.getKVValues(context,
							new QueryParams(this.properties.getWatch().getWaitTime(),
									currentIndex));

					// if response.value == null, response was a 404, otherwise it was a 200
					// reducing churn if there wasn't anything
					if (response.getValue() != null && !response.getValue().isEmpty()) {
						Long newIndex = response.getConsulIndex();

						if (newIndex != null && !newIndex.equals(currentIndex)) {
							// don't publish the same index again, don't publish the first time (-1) so index can be primed
							if (!this.consulIndexes.containsValue(newIndex) && !currentIndex.equals(-1L)) {
								RefreshEventData data = new RefreshEventData(context, currentIndex, newIndex);
								this.publisher.publishEvent(new RefreshEvent(this, data, data.toString()));
							}
							this.consulIndexes.put(context, newIndex);
						}
					}

				} catch (Exception e) {
					// only fail fast on the initial query, otherwise just log the error
					if (!initialized && this.properties.isFailFast()) {
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
		initialized = true;
	}

	@Override
	public void close() {
		this.running.compareAndSet(true, false);
	}

	/* for testing */ HashMap<String, Long> getConsulIndexes() {
		return this.consulIndexes;
	}

	@Data
	static class RefreshEventData {
		private final String context;
		private final Long prevIndex;
		private final Long newIndex;
	}
}
