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

package org.springframework.cloud.consul;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.consul.ConsulAutoConfiguration.BlockingQueryRequestFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link BlockingQueryRequestFactory}.
 *
 * @author Asish Kumar
 */
class BlockingQueryRequestFactoryTests {

	private final ClientHttpRequest defaultRequest = mock(ClientHttpRequest.class);

	private final ClientHttpRequest blockingRequest = mock(ClientHttpRequest.class);

	private final ClientHttpRequestFactory factory = new BlockingQueryRequestFactory((uri, method) -> defaultRequest,
			(uri, method) -> blockingRequest);

	@Test
	void requestWithWaitUsesBlockingQueryFactory() throws Exception {
		URI uri = URI.create("http://localhost:8500/v1/kv/config?wait=55s&index=10");

		assertThat(this.factory.createRequest(uri, HttpMethod.GET)).isSameAs(this.blockingRequest);
	}

	@Test
	void requestWithoutWaitUsesDefaultFactory() throws Exception {
		URI uri = URI.create("http://localhost:8500/v1/agent/self?dc=dc1");

		assertThat(this.factory.createRequest(uri, HttpMethod.GET)).isSameAs(this.defaultRequest);
	}

	@Test
	void blockingQueryTimeoutExceedsLongestWait() {
		// Consul caps the wait at 10 minutes and adds up to wait / 16 of jitter
		Duration maxWait = Duration.ofMinutes(10);
		assertThat(ConsulAutoConfiguration.BLOCKING_QUERY_READ_TIMEOUT)
			.isGreaterThan(maxWait.plus(maxWait.dividedBy(16)));
	}

}
