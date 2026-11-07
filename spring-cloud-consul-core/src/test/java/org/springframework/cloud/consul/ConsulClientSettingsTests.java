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

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.cloud.consul.ConsulAutoConfiguration.ConsulClientSettings;
import org.springframework.cloud.consul.ConsulProperties.TLSConfig;
import org.springframework.cloud.consul.model.http.KeyStoreInstanceType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the HTTP client settings created by {@link ConsulAutoConfiguration}.
 *
 * @author Asish Kumar
 */
class ConsulClientSettingsTests {

	private static final String BASE_URL = "http://localhost:8500";

	@Test
	void readTimeoutExceedsLongestBlockingQuery() {
		ConsulClientSettings settings = ConsulAutoConfiguration.createConsulClientSettings(BASE_URL, null);

		assertReadTimeoutExceedsLongestBlockingQuery(settings.httpClientSettings());
	}

	@Test
	void readTimeoutExceedsLongestBlockingQueryWithTls() {
		TLSConfig tls = new TLSConfig();
		tls.setKeyStoreInstanceType(KeyStoreInstanceType.JKS);
		tls.setKeyStorePath("src/test/resources/server.jks");
		tls.setKeyStorePassword("letmein");
		tls.setCertificatePath("src/test/resources/trustStore.jks");
		tls.setCertificatePassword("change_me");

		ConsulClientSettings settings = ConsulAutoConfiguration.createConsulClientSettings(BASE_URL, tls);

		assertThat(settings.httpClientSettings().sslBundle()).isNotNull();
		assertReadTimeoutExceedsLongestBlockingQuery(settings.httpClientSettings());
	}

	private void assertReadTimeoutExceedsLongestBlockingQuery(HttpClientSettings settings) {
		// Consul caps the wait at 10 minutes and adds up to wait / 16 of jitter
		Duration maxWait = Duration.ofMinutes(10);
		assertThat(settings.readTimeout()).isGreaterThan(maxWait.plus(maxWait.dividedBy(16)));
	}

}
