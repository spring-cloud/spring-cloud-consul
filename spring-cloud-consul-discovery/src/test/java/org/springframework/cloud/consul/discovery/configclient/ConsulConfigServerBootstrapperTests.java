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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.Bootstrapper;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.config.client.ConfigServerInstanceProvider;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsulConfigServerBootstrapperTests {

	@Test
	public void notEnabledDoesNotAddInstanceProviderFn() {
		new SpringApplicationBuilder(TestConfig.class)
				.properties("--server.port=0", "spring.cloud.service-registry.auto-registration.enabled=false")
				.addBootstrapper(registry -> registry.addCloseListener(event -> {
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					assertThat(providerFn).as("ConfigServerInstanceProvider.Function was created when it shouldn't")
							.isNull();
				})).run().close();
	}

	@Test
	public void enabledAddsInstanceProviderFn() {
		AtomicReference<ConsulDiscoveryClient> bootstrapDiscoveryClient = new AtomicReference<>();
		BindHandlerBootstrapper bindHandlerBootstrapper = new BindHandlerBootstrapper();
		ConfigurableApplicationContext context = new SpringApplicationBuilder(TestConfig.class)
				.properties("--server.port=0", "spring.cloud.config.discovery.enabled=true",
						"spring.cloud.consul.discovery.hostname=myhost",
						"spring.cloud.service-registry.auto-registration.enabled=false",
						"spring.cloud.consul.host=localhost")
				.addBootstrapper(bindHandlerBootstrapper)
				.addBootstrapper(registry -> registry.addCloseListener(event -> {
					bootstrapDiscoveryClient.set(event.getBootstrapContext().get(ConsulDiscoveryClient.class));
					ConfigServerInstanceProvider.Function providerFn = event.getBootstrapContext()
							.get(ConfigServerInstanceProvider.Function.class);
					assertThat(providerFn).as("ConfigServerInstanceProvider.Function was not created when it should.")
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

	static class BindHandlerBootstrapper implements Bootstrapper {

		private int onSuccessCount = 0;

		@Override
		public void intitialize(BootstrapRegistry registry) {
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
