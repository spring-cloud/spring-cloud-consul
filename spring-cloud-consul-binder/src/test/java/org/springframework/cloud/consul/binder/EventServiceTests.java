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

package org.springframework.cloud.consul.binder;

import java.util.List;

import org.junit.Test;
import tools.jackson.databind.ObjectMapper;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.binder.config.ConsulBinderProperties;
import org.springframework.cloud.consul.model.http.event.Event;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventServiceTests {

	@Test
	public void fireShouldSendAclToken() {
		ConsulBinderProperties properties = new ConsulBinderProperties();
		properties.setAclToken("test-acl-token");

		ConsulClient consul = mock(ConsulClient.class);
		Event event = new Event();
		when(consul.eventFire("test-event", "test-payload", "test-acl-token")).thenReturn(ResponseEntity.ok(event));

		EventService eventService = new EventService(properties, consul, new ObjectMapper());

		Event result = eventService.fire("test-event", "test-payload");

		assertThat(result).isSameAs(event);
		verify(consul).eventFire("test-event", "test-payload", "test-acl-token");
	}

	@Test
	public void getEventsResponseShouldSendAclToken() {
		ConsulBinderProperties properties = new ConsulBinderProperties();
		properties.setAclToken("test-acl-token");

		ConsulClient consul = mock(ConsulClient.class);
		when(consul.eventList(null, null, null, null, "test-acl-token")).thenReturn(ResponseEntity.ok(List.of()));

		EventService eventService = new EventService(properties, consul, new ObjectMapper());

		eventService.getEventsResponse();

		verify(consul).eventList(null, null, null, null, "test-acl-token");
	}

	@Test
	public void watchShouldSendAclToken() {
		ConsulBinderProperties properties = new ConsulBinderProperties();
		properties.setAclToken("test-acl-token");

		ConsulClient consul = mock(ConsulClient.class);
		when(consul.eventList(5L, -1L, null, null, null, null, "test-acl-token"))
			.thenReturn(ResponseEntity.ok(List.of()));

		EventService eventService = new EventService(properties, consul, new ObjectMapper());

		eventService.watch();

		verify(consul).eventList(5L, -1L, null, null, null, null, "test-acl-token");
	}

	@Test
	public void getAclTokenShouldReturnNullWhenTokenIsEmpty() {
		ConsulBinderProperties properties = new ConsulBinderProperties();
		properties.setAclToken("");

		EventService eventService = new EventService(properties, mock(ConsulClient.class), new ObjectMapper());

		assertThat(eventService.getAclToken()).isNull();
	}

}
