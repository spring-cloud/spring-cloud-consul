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

package org.springframework.cloud.consul.binder;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.binder.config.ConsulBinderProperties;
import org.springframework.cloud.consul.model.http.ConsulHeaders;
import org.springframework.cloud.consul.model.http.event.Event;
import org.springframework.http.ResponseEntity;

/**
 * @author Spencer Gibb
 */
public class EventService {

	protected ConsulBinderProperties properties;

	protected ConsulClient consul;

	protected ObjectMapper objectMapper = new ObjectMapper();

	private AtomicReference<Long> lastIndex = new AtomicReference<>();

	public EventService(ConsulBinderProperties properties, ConsulClient consul, ObjectMapper objectMapper) {
		this.properties = properties;
		this.consul = consul;
		this.objectMapper = objectMapper;
	}

	public ConsulClient getConsulClient() {
		return this.consul;
	}

	@PostConstruct
	public void init() {
		setLastIndex(getEventsResponse());
	}

	public Long getLastIndex() {
		return this.lastIndex.get();
	}

	private void setLastIndex(ResponseEntity<?> response) {
		String indexHeader = response.getHeaders().getFirst(ConsulHeaders.ConsulIndex.getHeaderName());
		Long consulIndex = indexHeader == null ? null : Long.parseLong(indexHeader);
		if (consulIndex != null) {
			this.lastIndex.set(consulIndex);
		}
	}

	public Event fire(String name, String payload) {
		ResponseEntity<Event> response = this.consul.eventFire(name, payload);
		return response.getBody();
	}

	public ResponseEntity<List<Event>> getEventsResponse() {
		return this.consul.eventList();
	}

	public List<Event> getEvents() {
		return getEventsResponse().getBody();
	}

	public List<Event> getEvents(Long lastIndex) {
		return filterEvents(readEvents(getEventsResponse()), lastIndex);
	}

	public List<Event> watch() {
		return watch(this.lastIndex.get());
	}

	public List<Event> watch(Long lastIndex) {
		// TODO: parameterized or configurable watch time
		long index = -1;
		if (lastIndex != null) {
			index = lastIndex;
		}
		int eventTimeout = 5;
		if (this.properties != null) {
			eventTimeout = this.properties.getEventTimeout();
		}
		ResponseEntity<List<Event>> watch = this.consul.eventList(eventTimeout, index);
		return filterEvents(readEvents(watch), lastIndex);
	}

	protected List<Event> readEvents(ResponseEntity<List<Event>> response) {
		setLastIndex(response);
		return response.getBody();
	}

	/**
	 * from https://github.com/hashicorp/consul/blob/master/watch/funcs.go#L169-L194 .
	 * @param toFilter events to filter
	 * @param lastIndex last index to pick from the list of events
	 * @return filtered list of events
	 */
	protected List<Event> filterEvents(List<Event> toFilter, Long lastIndex) {
		List<Event> events = toFilter;
		if (lastIndex != null) {
			for (int i = 0; i < events.size(); i++) {
				Event event = events.get(i);
				Long eventIndex = event.getWaitIndex();
				if (lastIndex.equals(eventIndex)) {
					events = events.subList(i + 1, events.size());
					break;
				}
			}
		}
		return events;
	}

}
