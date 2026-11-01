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

import org.junit.Test;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.event.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.GenericMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsulSendingHandlerTests {

	@Test
	public void handleMessageShouldSendAclToken() {
		ConsulClient consul = mock(ConsulClient.class);
		when(consul.eventFire("test-event", "test-payload", "test-acl-token"))
			.thenReturn(ResponseEntity.ok(new Event()));

		ConsulSendingHandler handler = new ConsulSendingHandler(consul, "test-event", "test-acl-token");

		handler.handleMessage(new GenericMessage<>("test-payload"));

		verify(consul).eventFire("test-event", "test-payload", "test-acl-token");
	}

}
