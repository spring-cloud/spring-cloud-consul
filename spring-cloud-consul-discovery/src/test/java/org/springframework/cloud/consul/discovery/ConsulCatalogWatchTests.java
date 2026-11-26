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

package org.springframework.cloud.consul.discovery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.ConsulClient.QueryParams;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Spencer Gibb
 */
public class ConsulCatalogWatchTests {

	@Test
	public void isRunningReportsCorrectly() {
		ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		ConsulCatalogWatch watch = new ConsulCatalogWatch(properties, null) {
			@Override
			public void catalogServicesWatch() {
				// do nothing
			}
		};
		assertThat(watch.isRunning()).isFalse();
		watch.start();
		assertThat(watch.isRunning()).isTrue();
		watch.stop();
		assertThat(watch.isRunning()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(ints = { -1, 0, 2 })
	public void catalogServicesWatchUsesOnlyPositiveTimeout(int configuredTimeout) {
		ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		properties.setCatalogServicesWatchTimeout(configuredTimeout);

		ConsulClient consul = mock(ConsulClient.class);
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

		ResponseEntity<Map<String, List<String>>> response = ResponseEntity.ok(Collections.emptyMap());

		given(consul.getCatalogServices(eq(properties.getAclToken()), any(QueryParams.class))).willReturn(response);

		ConsulCatalogWatch watch = new ConsulCatalogWatch(properties, consul);
		watch.setApplicationEventPublisher(publisher);

		watch.catalogServicesWatch();

		ArgumentCaptor<QueryParams> captor = ArgumentCaptor.forClass(QueryParams.class);
		verify(consul).getCatalogServices(eq(properties.getAclToken()), captor.capture());

		assertThat(captor.getValue().getWaitTime()).isEqualTo(configuredTimeout > 0 ? configuredTimeout : -1);
	}

}
