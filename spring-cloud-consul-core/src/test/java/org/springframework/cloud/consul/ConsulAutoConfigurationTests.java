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

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auto-configuration integration tests for {@link ConsulAutoConfiguration}.
 *
 * @author Chris Bono
 */
public class ConsulAutoConfigurationTests {

	private final ApplicationContextRunner appContextRunner = new ApplicationContextRunner()
			.withInitializer(new ConsulTestcontainers())
			.withConfiguration(AutoConfigurations.of(ConsulAutoConfiguration.class));

	@Test
	public void defaultConfiguration() {
		appContextRunner.run(context -> assertThat(context).hasNotFailed().hasSingleBean(ConsulProperties.class)
				.hasSingleBean(ConsulClient.class).hasSingleBean(ConsulHealthIndicator.class)
				.doesNotHaveBean(ConsulEndpoint.class));
	}

	@Test
	public void consulDisabled() {
		appContextRunner.withPropertyValues("spring.cloud.consul.enabled=false")
				.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConsulProperties.class)
						.doesNotHaveBean(ConsulClient.class).doesNotHaveBean(ConsulHealthIndicator.class)
						.doesNotHaveBean(ConsulEndpoint.class));
	}

	@Test
	public void tlsConfigured() {
		appContextRunner.withPropertyValues("spring.cloud.consul.tls.key-store-instance-type=JKS",
				"spring.cloud.consul.tls.key-store-path=src/test/resources/server.jks",
				"spring.cloud.consul.tls.key-store-password=letmein",
				"spring.cloud.consul.tls.certificate-path=src/test/resources/trustStore.jks",
				"spring.cloud.consul.tls.certificate-password=change_me").run(context -> {
					assertThat(context).hasNotFailed().hasSingleBean(ConsulClient.class);

					ConsulClient consulClient = context.getBean(ConsulClient.class);
					CatalogConsulClient client = (CatalogConsulClient) ReflectionTestUtils.getField(consulClient,
							"catalogClient");
					ConsulRawClient rawClient = (ConsulRawClient) ReflectionTestUtils.getField(client, "rawClient");
					HttpTransport httpTransport = (HttpTransport) ReflectionTestUtils.getField(rawClient,
							"httpTransport");
					assertThat(httpTransport).isInstanceOf(DefaultHttpsTransport.class);
				});
	}

	@Test
	public void nonActuatorAppGetsNoEndpointOrHealthIndicator() {
		appContextRunner.withClassLoader(new FilteredClassLoader(Endpoint.class))
				.withPropertyValues("management.endpoints.web.exposure.include=consul")
				.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConsulHealthIndicator.class)
						.doesNotHaveBean(ConsulEndpoint.class));
	}

	@Test
	public void consulEndpointAvailable() {
		appContextRunner.withPropertyValues("management.endpoints.web.exposure.include=consul")
				.run(context -> assertThat(context).hasNotFailed().hasSingleBean(ConsulEndpoint.class));
	}

	@Test
	public void consulEndpointAvailableButDisabled() {
		appContextRunner
				.withPropertyValues("management.endpoints.web.exposure.include=consul",
						"management.endpoint.consul.enabled=false")
				.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConsulEndpoint.class));
	}

	@Test
	public void consulEndpointDisabled() {
		appContextRunner.withPropertyValues("spring.cloud.consul.enabled=false")
				.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConsulProperties.class)
						.doesNotHaveBean(ConsulClient.class).doesNotHaveBean(ConsulHealthIndicator.class)
						.doesNotHaveBean(ConsulEndpoint.class));
	}

	@Test
	public void consulHealthIndicatorDisabled() {
		appContextRunner.withPropertyValues("management.health.consul.enabled=false")
				.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConsulHealthIndicator.class));
	}

}
