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

package org.springframework.cloud.consul.config;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
@DirtiesContext
public class ConsulConfigDataCustomizationIntegrationTests {

	private static final String APP_NAME = "testConsulConfigDataCustomization";

	private static final String PREFIX = "_configDataIntegrationTests_config__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static ConfigurableApplicationContext context;

	private static BindHandlerBootstrapper bindHandlerBootstrapper;

	@BeforeAll
	public static void setup() {
		ConsulTestcontainers.start();

		SpringApplication application = new SpringApplication(Config.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		bindHandlerBootstrapper = new BindHandlerBootstrapper();
		application.addBootstrapRegistryInitializer(bindHandlerBootstrapper);
		application.addBootstrapRegistryInitializer(ConsulBootstrapper.fromConsulProperties(consulProperties -> {
			try {
				return ConsulAutoConfiguration.createNewConsulClient(consulProperties);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}));
		application.addBootstrapRegistryInitializer(
				registry -> registry.register(ConsulBootstrapper.LoaderInterceptor.class, context1 -> loadContext -> {
					ConfigData configData = loadContext.getInvocation()
						.apply(loadContext.getLoaderContext(), loadContext.getResource());
					assertThat(configData).as("ConfigData was null for location %s", loadContext.getResource())
						.isNotNull();
					assertThat(configData.getPropertySources()).hasSize(1);
					PropertySource<?> propertySource = configData.getPropertySources().iterator().next();
					ConfigData.Options options = configData.getOptions(propertySource);
					assertThat(options)
						.as("ConfigData.options was null for location %s property source %s", loadContext.getResource(),
								propertySource.getName())
						.isNotNull();
					assertThat(options.contains(ConfigData.Option.IGNORE_IMPORTS)).isTrue();
					assertThat(options.contains(ConfigData.Option.IGNORE_PROFILES)).isTrue();
					boolean hasProfile = StringUtils.hasText(loadContext.getResource().getProfile());
					assertThat(options.contains(ConfigData.Option.PROFILE_SPECIFIC)).isEqualTo(hasProfile);
					return configData;
				}));
		context = application.run("--spring.application.name=" + APP_NAME,
				"--spring.config.import=consul:" + ConsulTestcontainers.getHost() + ":"
						+ ConsulTestcontainers.getPort(),
				"--spring.cloud.consul.config.prefixes=" + ROOT, "--spring.cloud.consul.config.watch.delay=10");

	}

	@AfterAll
	public static void teardown() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	void consulClientIsCustom() {
		ConsulClient client = context.getBean(ConsulClient.class);
		assertThat(client).isInstanceOf(ConsulClient.class);
		assertThat(bindHandlerBootstrapper.onSuccessCount).isGreaterThan(0);
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

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
