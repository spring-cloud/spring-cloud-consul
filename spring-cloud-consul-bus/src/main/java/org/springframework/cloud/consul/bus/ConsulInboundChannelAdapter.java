/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.consul.bus;

import static org.springframework.util.Base64Utils.decodeFromString;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.scheduling.annotation.Scheduled;

import com.ecwid.consul.v1.event.model.Event;

/**
 * Adapter that receives Messages from Consul Events, converts them into Spring
 * Integration Messages, and sends the results to a Message Channel.
 * @author Spencer Gibb
 */
public class ConsulInboundChannelAdapter extends MessageProducerSupport {
	@Autowired
	private EventService eventService;

	public ConsulInboundChannelAdapter() {
	}

	// link eventService to sendMessage
	/*
	 * Map<String, Object> headers =
	 * headerMapper.toHeadersFromRequest(message.getMessageProperties()); if
	 * (messageListenerContainer.getAcknowledgeMode() == AcknowledgeMode.MANUAL) {
	 * headers.put(AmqpHeaders.DELIVERY_TAG,
	 * message.getMessageProperties().getDeliveryTag()); headers.put(AmqpHeaders.CHANNEL,
	 * channel); }
	 * sendMessage(AmqpInboundChannelAdapter.this.getMessageBuilderFactory().withPayload
	 * (payload).copyHeaders(headers).build());
	 */

	// start thread
	// make blocking calls
	// foreach event -> send message

	@Override
	protected void doStart() {
	}

	@Scheduled(fixedDelayString = "${spring.cloud.consul.bus.eventDelay:30000}")
	public void getEvents() throws IOException {
		List<Event> events = eventService.watch();
		for (Event event : events) {
			// Map<String, Object> headers = new HashMap<>();
			// headers.put(MessageHeaders.REPLY_CHANNEL, outputChannel.)
			String decoded = new String(decodeFromString(event.getPayload()));
			sendMessage(getMessageBuilderFactory().withPayload(decoded)
			// TODO: support headers
					.build());
		}
	}

	@Override
	protected void doStop() {
	}
}
