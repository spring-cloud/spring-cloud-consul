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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConsulDiscoveryClient} probe API.
 */
@ExtendWith(MockitoExtension.class)
public class ConsulDiscoveryClientProbeTests {

	@Mock
	private ConsulClient consulClient;

	@Mock
	private ConsulDiscoveryProperties consulDiscoveryProperties;

	@InjectMocks
	private ConsulDiscoveryClient discoveryClient;

	@Test
	public void probeSucceeds() {
		when(consulClient.getStatusLeader()).thenReturn(ResponseEntity.ok("5150"));
		discoveryClient.probe();
		verify(consulClient).getStatusLeader();
	}

	@Test
	public void probeFails() {
		when(consulClient.getStatusLeader())
			.thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "No leader"));
		assertThatThrownBy(() -> discoveryClient.probe()).isInstanceOf(HttpClientErrorException.class)
			.hasMessageContaining("No leader");
		verify(consulClient).getStatusLeader();
	}

}
