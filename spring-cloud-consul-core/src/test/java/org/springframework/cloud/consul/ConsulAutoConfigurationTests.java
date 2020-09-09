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

package org.springframework.cloud.consul;

import com.ecwid.consul.transport.DefaultHttpsTransport;
import com.ecwid.consul.transport.HttpTransport;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.cloud.consul.tls.key-store-instance-type=JKS",
		"spring.cloud.consul.tls.key-store-path=src/test/resources/server.jks",
		"spring.cloud.consul.tls.key-store-password=letmein",
		"spring.cloud.consul.tls.certificate-path=src/test/resources/trustStore.jks",
		"spring.cloud.consul.tls.certificate-password=change_me" })
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoConfigurationTests {

	@Autowired
	private ConsulClient consulClient;

	@Test
	public void tlsConfigured() {
		CatalogConsulClient client = (CatalogConsulClient) ReflectionTestUtils
				.getField(this.consulClient, "catalogClient");
		ConsulRawClient rawClient = (ConsulRawClient) ReflectionTestUtils.getField(client,
				"rawClient");
		HttpTransport httpTransport = (HttpTransport) ReflectionTestUtils
				.getField(rawClient, "httpTransport");
		assertThat(httpTransport).isInstanceOf(DefaultHttpsTransport.class);
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	protected static class TestConfig {

	}

}
