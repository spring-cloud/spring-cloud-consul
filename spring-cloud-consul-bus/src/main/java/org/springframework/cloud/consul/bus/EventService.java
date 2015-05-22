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

package org.springframework.cloud.consul.bus;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	protected ConsulBusProperties properties;

	@Autowired
	protected ConsulClient consul;

	@Autowired(required = false)
	protected ObjectMapper objectMapper = new ObjectMapper();

	private AtomicReference<BigInteger> lastIndex = new AtomicReference<>();

	@PostConstruct
	public void init() {
		setLastIndex(getEventsResponse());
	}

	private void setLastIndex(Response<?> response) {
		Long consulIndex = response.getConsulIndex();
		if (consulIndex != null) {
			lastIndex.set(BigInteger.valueOf(consulIndex));
		}
	}

	public BigInteger getLastIndex() {
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

	/**
	 * from https://github.com/hashicorp/consul/blob/master/api/event.go#L90-L104 //
	 * IDToIndex is a bit of a hack. This simulates the index generation to // convert an
	 * event ID into a WaitIndex. func (e *Event) IDToIndex(uuid string) uint64 { lower :=
	 * uuid[0:8] + uuid[9:13] + uuid[14:18] upper := uuid[19:23] + uuid[24:36] lowVal, err
	 * := strconv.ParseUint(lower, 16, 64) if err != nil { panic("Failed to convert " +
	 * lower) } highVal, err := strconv.ParseUint(upper, 16, 64) if err != nil {
	 * panic("Failed to convert " + upper) } return lowVal ^ highVal //^ bitwise XOR
	 * integers }
	 */
	public BigInteger toIndex(String eventId) {
		String lower = eventId.substring(0, 8) + eventId.substring(9, 13)
				+ eventId.substring(14, 18);
		String upper = eventId.substring(19, 23) + eventId.substring(24, 36);
		BigInteger lowVal = new BigInteger(lower, 16);
		BigInteger highVal = new BigInteger(upper, 16);
		BigInteger index = lowVal.xor(highVal);
		return index;
	}

	public List<Event> getEvents(BigInteger lastIndex) {
		return filterEvents(readEvents(getEventsResponse()), lastIndex);
	}

	public List<Event> watch() {
		return watch(lastIndex.get());
	}

	public List<Event> watch(BigInteger lastIndex) {
		// TODO: parameterized or configurable watch time
		long index = -1;
		if (lastIndex != null) {
			index = lastIndex.longValue();
		}
		Response<List<Event>> watch = consul.eventList(new QueryParams(properties.eventTimeout, index));
		return filterEvents(readEvents(watch), lastIndex);
	}

	protected List<Event> readEvents(Response<List<Event>> response) {
		setLastIndex(response);
		return response.getValue();
	}

	/**
	 * from https://github.com/hashicorp/consul/blob/master/watch/funcs.go#L169-L194
	 */
	protected List<Event> filterEvents(List<Event> toFilter, BigInteger lastIndex) {
		List<Event> events = toFilter;
		if (lastIndex != null) {
			for (int i = 0; i < events.size(); i++) {
				Event event = events.get(i);
				BigInteger eventIndex = toIndex(event.getId());
				if (eventIndex.equals(lastIndex)) {
					events = events.subList(i + 1, events.size());
					break;
				}
			}
		}
		return events;
	}

}
