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

import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;

/**
 * Adapter that converts and sends Messages as Consul events
 * @author Spencer Gibb
 */
public class ConsulSendingHandler extends AbstractMessageHandler {

	private final ConsulClient consul;
	private final String eventName;

	public ConsulSendingHandler(ConsulClient consul, String eventName) {
		this.consul = consul;
		this.eventName = eventName;
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("Publishing message" + message);
		}

		Object payload = message.getPayload();
		// TODO: support headers
		// TODO: support consul event filters: NodeFilter, ServiceFilter, TagFilter
		Response<Event> event = consul.eventFire(this.eventName, (String) payload,
				new EventParams(), QueryParams.DEFAULT);
		// TODO: return event?
		// return null;
	}
}
