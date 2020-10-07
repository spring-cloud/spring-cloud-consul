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

import com.ecwid.consul.v1.OperationException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Spencer Gibb
 */
public class ConsulInboundMessageProducerTests {

	@Test
	public void getEventsShouldNotThrowException() {
		EventService eventService = mock(EventService.class);
		when(eventService.watch()).thenThrow(new OperationException(500, "error", ""));

		ConsulInboundMessageProducer producer = new ConsulInboundMessageProducer(eventService);

		try {
			producer.getEvents();
		}
		catch (Exception e) {
			fail("ConsulInboundMessageProducer threw unexpected exception: " + e);
		}

	}

}
