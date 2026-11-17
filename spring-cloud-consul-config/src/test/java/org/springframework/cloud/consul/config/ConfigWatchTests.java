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

package org.springframework.cloud.consul.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.ConsulHeaders;
import org.springframework.cloud.consul.model.http.kv.GetValue;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
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
		this.configProperties = new ConsulConfigProperties();
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

		verify(eventPublisher, atLeastOnce()).publishEvent(any(RefreshEvent.class));
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

		this.configProperties.setFormat(FILES);
		setupWatch(eventPublisher, new GetValue(), "/config/app.yml");

		verify(eventPublisher, atLeastOnce()).publishEvent(any(RefreshEvent.class));
	}

	private void setupWatch(ApplicationEventPublisher eventPublisher, GetValue getValue, String context) {
		setupWatch(eventPublisher, getValue, context, null);
	}

	private void setupWatch(ApplicationEventPublisher eventPublisher, GetValue getValue, String context,
			String aclToken) {
		ConsulClient consul = mock(ConsulClient.class);
		List<GetValue> getValues = null;

		BodyBuilder response;
		if (getValue != null) {
			getValues = Arrays.asList(getValue);
			response = ResponseEntity.status(HttpStatus.OK);
		}
		else {
			response = ResponseEntity.status(HttpStatus.NOT_FOUND);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "1");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");
		response.headers(headers);
		String normalizedContext = ConfigWatch.normalizeContext(context, this.configProperties);

		when(consul.getKVValues(eq(normalizedContext), nullable(String.class), anyLong(), anyLong()))
			.thenReturn(response.body(getValues));

		if (StringUtils.hasText(aclToken)) {
			this.configProperties.setAclToken(aclToken);
		}

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(normalizedContext, 0L);
		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();
	}

	@Test
	public void watchPublishesEventWhenConfigIsDeleted() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");

		ResponseEntity<List<GetValue>> response = ResponseEntity.ok().headers(headers).body(Collections.emptyList());

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes,
				Collections.singleton(context), getTaskSchedulerForTest());
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventWhenConfigIsDeletedAndRefreshOnDeleteIsDisabled() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");

		ResponseEntity<List<GetValue>> response = ResponseEntity.ok().headers(headers).body(Collections.emptyList());

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, never()).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventWhenEmptyContextIsNeverPopulated() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");

		ResponseEntity<List<GetValue>> response = ResponseEntity.ok().headers(headers).body(Collections.emptyList());

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes, Collections.emptySet(),
				getTaskSchedulerForTest());
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, never()).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventWhenNeverPopulatedContextIsNotFound() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");

		ResponseEntity<List<GetValue>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes, Collections.emptySet(),
				getTaskSchedulerForTest());
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, never()).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void firstCallDoesNotPublishEvent() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
		this.configProperties.setFormat(FILES);

		GetValue getValue = new GetValue();
		String context = "/config/app.yml";
		ConsulClient consul = mock(ConsulClient.class);
		List<GetValue> getValues = Collections.singletonList(getValue);

		ResponseEntity<List<GetValue>> response = ResponseEntity.ok(getValues);
		when(consul.getKVValues(eq(context), anyString(), anyLong(), anyLong())).thenReturn(response);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, new LinkedHashMap<>());
		watch.setApplicationEventPublisher(eventPublisher);

		watch.watchConfigKeyValues();
		verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchPublishesEventWhenAllConfigKeysAreDeleted() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");

		ResponseEntity<List<GetValue>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		TaskScheduler taskScheduler = mock(TaskScheduler.class);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes,
				Collections.singleton(context), taskScheduler);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventWhenNotFoundAndRefreshOnDeleteIsDisabled() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");

		ResponseEntity<List<GetValue>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventWhenNotFoundAndIndexIsInitiallyUnprimed() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");

		ResponseEntity<List<GetValue>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, -1L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		TaskScheduler taskScheduler = mock(TaskScheduler.class);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes, Collections.emptySet(),
				taskScheduler);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();
		watch.watchConfigKeyValues();

		org.mockito.ArgumentCaptor<Long> indexCaptor = org.mockito.ArgumentCaptor.forClass(Long.class);
		verify(consul, times(2)).getKVValues(eq(context), nullable(String.class), anyLong(), indexCaptor.capture());

		assertThat(indexCaptor.getAllValues()).containsExactly(-1L, 2L);
		verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventWhenNotFoundAndIndexHasNotChanged() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "2");

		ResponseEntity<List<GetValue>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 2L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, times(0)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishDuplicateEventsForSameIndexAcrossContexts() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String firstContext = "app/";
		String secondContext = "application/";

		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders headers = new HttpHeaders();
		headers.add(ConsulHeaders.INDEX_HEADER, "3");
		headers.add(ConsulHeaders.LAST_CONTACT_HEADER, "1");

		ResponseEntity<List<GetValue>> response = ResponseEntity.ok()
			.headers(headers)
			.body(Collections.singletonList(mock(GetValue.class)));

		when(consul.getKVValues(eq(firstContext), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		when(consul.getKVValues(eq(secondContext), nullable(String.class), anyLong(), anyLong())).thenReturn(response);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(firstContext, 1L);
		initialIndexes.put(secondContext, 2L);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes);
		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();

		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void watchDoesNotPublishEventForConsecutiveNotFoundResponses() {
		ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

		String context = "app/";
		ConsulClient consul = mock(ConsulClient.class);

		HttpHeaders firstHeaders = new HttpHeaders();
		firstHeaders.add(ConsulHeaders.INDEX_HEADER, "2");

		ResponseEntity<List<GetValue>> firstResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
			.headers(firstHeaders)
			.build();

		HttpHeaders secondHeaders = new HttpHeaders();
		secondHeaders.add(ConsulHeaders.INDEX_HEADER, "3");

		ResponseEntity<List<GetValue>> secondResponse = ResponseEntity.status(HttpStatus.NOT_FOUND)
			.headers(secondHeaders)
			.build();

		when(consul.getKVValues(eq(context), nullable(String.class), anyLong(), anyLong())).thenReturn(firstResponse,
				secondResponse);

		LinkedHashMap<String, Long> initialIndexes = new LinkedHashMap<>();
		initialIndexes.put(context, 1L);

		this.configProperties.getWatch().setRefreshOnDelete(true);

		ConfigWatch watch = new ConfigWatch(this.configProperties, consul, initialIndexes,
				Collections.singleton(context), mock(TaskScheduler.class));

		watch.setApplicationEventPublisher(eventPublisher);
		watch.start();

		watch.watchConfigKeyValues();
		watch.watchConfigKeyValues();

		verify(consul, times(2)).getKVValues(eq(context), nullable(String.class), anyLong(), anyLong());
		verify(eventPublisher, times(1)).publishEvent(any(RefreshEvent.class));
	}

	@Test
	public void refreshOnDeleteIsDisabledByDefault() {
		assertThat(this.configProperties.getWatch().isRefreshOnDelete()).isFalse();
	}

	@Test
	public void normalizeContextRemovesLeadingSlash() {
		assertThat(ConfigWatch.normalizeContext("/config/application/", this.configProperties))
			.isEqualTo("config/application/");
	}

	@Test
	public void normalizeContextPreservesContextWithoutLeadingSlash() {
		assertThat(ConfigWatch.normalizeContext("config/application/", this.configProperties))
			.isEqualTo("config/application/");
	}

	private ThreadPoolTaskScheduler getTaskSchedulerForTest() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.initialize();
		return taskScheduler;
	}

}
