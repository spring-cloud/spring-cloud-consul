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

package org.springframework.cloud.consul.binder;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.springframework.cloud.consul.binder.config.ConsulBinderProperties;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		return consul;
	}

	@PostConstruct
	public void init() {
		setLastIndex(getEventsResponse());
	}

	private void setLastIndex(Response<?> response) {
		Long consulIndex = response.getConsulIndex();
		if (consulIndex != null) {
			lastIndex.set(response.getConsulIndex());
		}
	}

	public Long getLastIndex() {
		return lastIndex.get();
	}

	public Event fire(String name, String payload) {
		Response<Event> response = consul.eventFire(name, payload, new EventParams(),
				QueryParams.DEFAULT);
		return response.getValue();
	}

	public Response<List<Event>> getEventsResponse() {
		return consul.eventList(QueryParams.DEFAULT);
	}

	public List<Event> getEvents() {
		return getEventsResponse().getValue();
	}

	public List<Event> getEvents(Long lastIndex) {
		return filterEvents(readEvents(getEventsResponse()), lastIndex);
	}

	public List<Event> watch() {
		return watch(lastIndex.get());
	}

	public List<Event> watch(Long lastIndex) {
		// TODO: parameterized or configurable watch time
		long index = -1;
		if (lastIndex != null) {
			index = lastIndex;
		}
		int eventTimeout = 5;
		if (properties != null) {
			eventTimeout = properties.getEventTimeout();
		}
		Response<List<Event>> watch = consul.eventList(new QueryParams(eventTimeout, index));
		return filterEvents(readEvents(watch), lastIndex);
	}

	protected List<Event> readEvents(Response<List<Event>> response) {
		setLastIndex(response);
		return response.getValue();
	}

	/**
	 * from https://github.com/hashicorp/consul/blob/master/watch/funcs.go#L169-L194
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
