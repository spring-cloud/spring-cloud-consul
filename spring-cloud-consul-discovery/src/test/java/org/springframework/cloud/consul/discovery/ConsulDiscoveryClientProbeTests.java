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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConsulDiscoveryClient} probe API.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConsulDiscoveryClientProbeTests {

	@Mock
	private ConsulClient consulClient;

	@Mock
	private ConsulDiscoveryProperties consulDiscoveryProperties;

	@InjectMocks
	private ConsulDiscoveryClient discoveryClient;

	@Test
	public void probeSucceeds() {
		when(consulClient.getStatusLeader())
				.thenReturn(new Response<>("5150", 5150L, false, System.currentTimeMillis()));
		discoveryClient.probe();
		verify(consulClient).getStatusLeader();
	}

	@Test
	public void probeFails() {
		when(consulClient.getStatusLeader()).thenThrow(new OperationException(5150, "5150", "No leader"));
		assertThatThrownBy(() -> discoveryClient.probe()).isInstanceOf(OperationException.class)
				.hasMessageContaining("No leader");
		verify(consulClient).getStatusLeader();
	}

}
