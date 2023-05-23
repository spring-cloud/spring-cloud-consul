/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.consul.discovery.configclient;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.ecwid.consul.transport.TransportException;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class ConsulConfigServerBootstrapperTests {

	@Test
	public void notEnabledDoesNotAddInstanceProviderFn() {
		new SpringApplicationBuilder(TestConfig.class)
				.properties("--server.port=0", "spring.cloud.service-registry.auto-registration.enabled=false")
				.addBootstrapRegistryInitializer(registry -> registry.addCloseListener(event -> {
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					Log log = mock(Log.class);
					assertThat(providerFn.apply("id", event.getBootstrapContext().get(Binder.class),
							event.getBootstrapContext().get(BindHandler.class), log))
									.as("ConfigServerInstanceProvider.Function should return empty list")
									.isEqualTo(Collections.EMPTY_LIST);
				})).run().close();
	}

	@Test
	public void consulDiscoveryClientNotEnabledProvidesEmptyList() {
		new SpringApplicationBuilder(TestConfig.class)
				.properties("--server.port=0", "spring.cloud.service-registry.auto-registration.enabled=false")
				.addBootstrapRegistryInitializer(registry -> registry.addCloseListener(event -> {
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					Log log = mock(Log.class);
					assertThat(providerFn.apply("id", event.getBootstrapContext().get(Binder.class),
							event.getBootstrapContext().get(BindHandler.class), log))
									.as("ConfigServerInstanceProvider.Function should return empty list")
									.isEqualTo(Collections.EMPTY_LIST);
				})).run().close();
	}

	@Test
	public void springCloudDiscoveryClientNotEnabledProvidesEmptyList() {
		new SpringApplicationBuilder(TestConfig.class)
				.properties("--server.port=0", "spring.cloud.service-registry.auto-registration.enabled=false")
				.addBootstrapRegistryInitializer(registry -> registry.addCloseListener(event -> {
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					Log log = mock(Log.class);
					assertThat(providerFn.apply("id", event.getBootstrapContext().get(Binder.class),
							event.getBootstrapContext().get(BindHandler.class), log))
									.as("ConfigServerInstanceProvider.Function should return empty list")
									.isEqualTo(Collections.EMPTY_LIST);
				})).run().close();
	}

	@Test
	public void enabledAddsInstanceProviderFn() {
		AtomicReference<ConsulDiscoveryClient> bootstrapDiscoveryClient = new AtomicReference<>();
		BindHandlerBootstrapper bindHandlerBootstrapper = new BindHandlerBootstrapper();
		ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfig.class)
				.properties("--server.port=0", "spring.cloud.config.discovery.enabled=true",
						"spring.cloud.consul.discovery.hostname=myhost",
						"spring.cloud.service-registry.auto-registration.enabled=false")
				.addBootstrapRegistryInitializer(bindHandlerBootstrapper)
				.addBootstrapRegistryInitializer(registry -> registry.addCloseListener(event -> {
					bootstrapDiscoveryClient.set(event.getBootstrapContext().get(ConsulDiscoveryClient.class));
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					assertThatThrownBy(() -> providerFn.apply("id", event.getBootstrapContext().get(Binder.class),
							event.getBootstrapContext().get(BindHandler.class), mock(Log.class)))
									.isInstanceOf(TransportException.class)
									.hasMessageContaining(
											"org.apache.http.conn.HttpHostConnectException: Connect to localhost:8500")
									.as("Should have tried to reach out to Consul to get config server instance")
									.isNotNull();
				})).run();
		ConsulDiscoveryClient discoveryClient = context.getBean(ConsulDiscoveryClient.class);
		assertThat(discoveryClient == bootstrapDiscoveryClient.get()).isTrue();
		assertThat(bindHandlerBootstrapper.onSuccessCount).isGreaterThan(0);
		context.close();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfig {

	}

	static class BindHandlerBootstrapper implements BootstrapRegistryInitializer {

		private int onSuccessCount = 0;

		@Override
		public void initialize(BootstrapRegistry registry) {
			registry.register(BindHandler.class, context -> new BindHandler() {
				@Override
				public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context,
						Object result) {
					onSuccessCount++;
					return result;
				}
			});
		}

	}

}
