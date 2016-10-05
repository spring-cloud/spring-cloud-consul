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

package org.springframework.cloud.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * @author Spencer Gibb
 */
public class ConfigWatchTests {

	private ConsulConfigProperties configProperties;

	@Before
	public void setUp() throws Exception {
		configProperties = new ConsulConfigProperties();
	}

	@Test
	public void watchPublishesEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		setupWatch(eventPublisher, new GetValue(), configProperties, "/app/");

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchWithNullValueDoesNotPublishEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		setupWatch(eventPublisher, null, configProperties, "/app/");

		verify(eventPublisher, never()).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchForFileFormatPublishesEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		configProperties.setFormat(FILES);
		setupWatch(eventPublisher, new GetValue(), configProperties, "/config/app.yml" );

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	private void setupWatch(ApplicationEventPublisher eventPublisher, GetValue getValue, ConsulConfigProperties configProperties, String context ) {
		ConsulClient consul = mock(ConsulClient.class);
		List<GetValue> getValues = null;

		if (getValue != null) {
			getValues = Arrays.asList(getValue);
		}

		Response<List<GetValue>> response = new Response<>(getValues, 1L, false, 1L);
		when(consul.getKVValues(eq(context), any(QueryParams.class))).thenReturn(response);

		ConfigWatch watch = new ConfigWatch(configProperties, Arrays.asList(context), consul);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.getConsulIndexes().put(context, 0L);
		watch.start();

		watch.watchConfigKeyValues();
	}

}
