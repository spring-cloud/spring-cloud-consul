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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ConfigWatch implements Closeable, ApplicationEventPublisherAware {

	private final ConsulConfigProperties properties;
	private final ConsulClient consul;
	private LinkedHashMap<String, Long> consulIndexes;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private ApplicationEventPublisher publisher;
	private boolean firstTime = true;

	@Deprecated
	public ConfigWatch(ConsulConfigProperties properties, List<String> contexts, ConsulClient consul) {
		this(properties, consul, new LinkedHashMap<String, Long>());
	}

	public ConfigWatch(ConsulConfigProperties properties, ConsulClient consul, LinkedHashMap<String, Long> initialIndexes) {
		this.properties = properties;
		this.consul = consul;
		this.consulIndexes = new LinkedHashMap<>(initialIndexes);
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
								RefreshEventData data = new RefreshEventData(context, currentIndex, newIndex);
								this.publisher.publishEvent(new RefreshEvent(this, data, data.toString()));
							}
							this.consulIndexes.put(context, newIndex);
						}
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

	@Override
	public void close() {
		this.running.compareAndSet(true, false);
	}

	@Data
	static class RefreshEventData {
		private final String context;
		private final Long prevIndex;
		private final Long newIndex;
	}
}
