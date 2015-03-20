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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.bus.BusAutoConfiguration;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.bus.jackson.SubtypeModule;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Spencer Gibb
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsulBusIT {

	@Test
	public void test001ConsulOutboundEndpoint_HandleRequestMessage() {
		ConfigurableApplicationContext context = getOutboundContext();
		context.publishEvent(new SimpleRemoteEvent(this, "testService", "testMessage"));
	}

	private ConfigurableApplicationContext getOutboundContext() {
		System.setProperty("spring.cloud.config.enabled", "false");
		ConfigurableApplicationContext context = new SpringApplicationBuilder()
				.web(false).sources(OutboundConfig.class).run();
		context.setId("testService");
		return context;
	}

	/*
	 * @Test public void test002ConsulInboundChannelAdapter() {
	 * ConfigurableApplicationContext inbound = getInboundContext();
	 * ConfigurableApplicationContext outbound = getOutboundContext();
	 * outbound.publishEvent(new TestMessage(this, "testService", "inboundTestService",
	 * "testMessage"));
	 * 
	 * InboundConfig inboundConfig = inbound.getBean(InboundConfig.class);
	 * assertNotNull("message was null", inboundConfig.message); }
	 * 
	 * private ConfigurableApplicationContext getInboundContext() {
	 * System.setProperty("spring.cloud.config.enabled", "false");
	 * ConfigurableApplicationContext context = new SpringApplicationBuilder() .web(false)
	 * .sources(InboundConfig.class) .run(); context.setId("inboundTestService"); return
	 * context; }
	 */

	protected static final String JSON_PAYLOAD = "{\"type\":\"simple\",\"timestamp\":1416349427372,\"originService\":\"testService\",\"destinationService\":null,\"message\":\"testMessage\"}";

	@Test
	public void test003JsonToObject() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new SubtypeModule(SimpleRemoteEvent.class));
		JsonToObjectTransformer transformer = Transformers.fromJson(
				RemoteApplicationEvent.class, new Jackson2JsonObjectMapper(objectMapper));
		/*
		 * HashMap<String, Object> map = new HashMap<>(); map.put(JsonHeaders.TYPE_ID,
		 * RemoteApplicationEvent.class);
		 */
		Message<?> message = transformer.transform(new GenericMessage<>(JSON_PAYLOAD));
		Object payload = message.getPayload();
		assertTrue("payload is of wrong type", payload instanceof RemoteApplicationEvent);
		assertTrue("payload is of wrong type", payload instanceof SimpleRemoteEvent);
		SimpleRemoteEvent event = (SimpleRemoteEvent) payload;
		assertEquals("payload is wrong", "testMessage", event.getMessage());
	}

	@Configuration
	@Import({ ConsulAutoConfiguration.class, BusAutoConfiguration.class,
			ConsulBusAutoConfiguration.class })
	@EnableIntegration
	public static class OutboundConfig {

		@Bean
		public ObjectMapper objectMapper() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new SubtypeModule(SimpleRemoteEvent.class));
			return objectMapper;
		}
	}

	@Configuration
	@Import({ ConsulAutoConfiguration.class, BusAutoConfiguration.class,
			ConsulBusAutoConfiguration.class })
	@EnableIntegration
	public static class InboundConfig implements
			ApplicationListener<RemoteApplicationEvent> {
		RemoteApplicationEvent message;

		@Override
		public void onApplicationEvent(RemoteApplicationEvent event) {
			this.message = event;
		}
	}
}
