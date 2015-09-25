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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bus.BusAutoConfiguration;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnConsulEnabled
@ConditionalOnProperty(value = "spring.cloud.consul.bus.enabled", matchIfMissing = true)
@AutoConfigureAfter(BusAutoConfiguration.class)
@EnableScheduling
@EnableConfigurationProperties
public class ConsulBusAutoConfiguration {
	@Autowired
	@Qualifier("cloudBusInboundChannel")
	MessageChannel cloudBusInboundChannel;

	@Autowired
	ObjectMapper objectMapper;

	@Bean
	public ConsulBusProperties consulBusProperties() {
		return new ConsulBusProperties();
	}

	@Bean
	public EventService eventService() {
		return new EventService();
	}

	@Bean
	public ConsulOutboundEndpoint consulOutboundEndpoint() {
		return new ConsulOutboundEndpoint();
	}

	@Bean
	public IntegrationFlow cloudBusConsulOutboundFlow(
			@Qualifier("cloudBusOutboundChannel") MessageChannel cloudBusOutboundChannel) {
		return IntegrationFlows.from(cloudBusOutboundChannel)
		// TODO: put the json headers as part of the message, here?
				.transform(Transformers.toJson()).handle(consulOutboundEndpoint()).get();
	}

	@Bean
	public IntegrationFlow cloudBusConsulInboundFlow() {
		return IntegrationFlows
				.from(consulInboundChannelAdapter())
				.transform(
                        Transformers.fromJson(RemoteApplicationEvent.class,
                                new Jackson2JsonObjectMapper(objectMapper)))
				.channel(cloudBusInboundChannel) // now set in consulInboundChannelAdapter
													// bean
				.get();
	}

	@Bean
	public ConsulInboundChannelAdapter consulInboundChannelAdapter() {
		ConsulInboundChannelAdapter adapter = new ConsulInboundChannelAdapter();
		adapter.setOutputChannel(cloudBusInboundChannel);
		return adapter;
	}

}
