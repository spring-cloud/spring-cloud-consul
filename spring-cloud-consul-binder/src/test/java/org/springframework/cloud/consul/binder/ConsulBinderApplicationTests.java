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

import java.util.concurrent.TimeUnit;

import com.ecwid.consul.v1.ConsulClient;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulBinderApplicationTests.Application.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulBinderApplicationTests {

	@Rule
	public final WireMockRule wireMock = new WireMockRule(18500);

	@Autowired
	private Events events;

	@Before
	public void setUp() throws Exception {

		this.wireMock.stubFor(put(urlPathMatching("/v1/event/fire/purchases")).willReturn(aResponse().withStatus(200)));

		/*
		 * wireMock.stubFor(get(urlPathMatching("/v1/event/list"))
		 * .willReturn(aResponse().withBody("[]") .withStatus(200)
		 * .withHeader("X-Consul-Index", "1")));
		 */
	}

	@Test
	public void shouldInitializeConsulSource() {

		assertThat(this.events).isNotNull();
	}

	@Test
	public void shouldPublishTextConsulMessage() {

		// given
		final Message<String> message = MessageBuilder.withPayload("Hello Consul!").build();

		// when
		this.events.purchases().send(message);

		// then
		await().atMost(1, TimeUnit.SECONDS);
		verify(1, putRequestedFor(urlPathMatching("/v1/event/fire/purchases")));
	}

	interface Events {

		@Output
		MessageChannel purchases();

	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EnableBinding(Events.class)
	public static class Application {

		@Bean
		public ConsulClient consulClient() {
			return new ConsulClient("localhost", 18500);
		}

		@Bean
		public EventService eventService(ConsulClient consulClient) {
			EventService eventService = mock(EventService.class);
			when(eventService.getConsulClient()).thenReturn(consulClient);
			return eventService;
		}

	}

}
