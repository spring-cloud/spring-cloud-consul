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
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
	public void watchPublishesEventWithAcl() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		setupWatch(eventPublisher, new GetValue(), "/app/", "2ee647bd-bd69-4118-9f34-b9a6e9e60746");

		verify(eventPublisher, atLeastOnce()).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchPublishesEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		setupWatch(eventPublisher, new GetValue(), "/app/");

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchWithNullValueDoesNotPublishEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		setupWatch(eventPublisher, null, "/app/");

		verify(eventPublisher, never()).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchForFileFormatPublishesEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		configProperties.setFormat(FILES);
		setupWatch(eventPublisher, new GetValue(), "/config/app.yml" );

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	private void setupWatch(ApplicationEventPublisher eventPublisher, GetValue getValue, String context) {
		setupWatch(eventPublisher, getValue, context, null);
	}

	private void setupWatch(ApplicationEventPublisher eventPublisher, GetValue getValue, String context, String aclToken) {
		ConsulClient consul = mock(ConsulClient.class);
		List<GetValue> getValues = null;

		if (getValue != null) {
			getValues = Arrays.asList(getValue);
		}

		Response<List<GetValue>> response = new Response<>(getValues, 1L, false, 1L);
		when(consul.getKVValues(eq(context), nullable(String.class), any(QueryParams.class))).thenReturn(response);

		if (StringUtils.hasText(aclToken)) {
			configProperties.setAclToken(aclToken);
		}

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 0L);
		ConfigWatch watch = new ConfigWatch(configProperties, consul, initialIndexes);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();
	}

	@Test
	public void firstCallDoesNotPublishEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
		configProperties.setFormat(FILES);

		GetValue getValue = new GetValue();
		String context = "/config/app.yml";
		ConsulClient consul = mock(ConsulClient.class);
		List<GetValue> getValues = Collections.singletonList(getValue);

		Response<List<GetValue>> response = new Response<>(getValues, 1L, false, 1L);
		when(consul.getKVValues(eq(context), anyString(), any(QueryParams.class))).thenReturn(response);

		ConfigWatch watch = new ConfigWatch(configProperties, consul, new LinkedHashMap<String, Long>());
		watch.setApplicationEventPublisher(eventPublisher);

		watch.watchConfigKeyValues();
		verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
	}

}
