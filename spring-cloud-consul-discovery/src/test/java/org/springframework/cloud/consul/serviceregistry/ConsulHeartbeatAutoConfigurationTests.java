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

package org.springframework.cloud.consul.serviceregistry;

import com.ecwid.consul.v1.ConsulClient;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Auto-configuration tests for {@link ConsulHeartbeatAutoConfiguration}
 */
class ConsulHeartbeatAutoConfigurationTests {

	private ApplicationContextRunner appContextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ConsulHeartbeatAutoConfiguration.class))
		.withBean(ConsulClient.class, () -> mock(ConsulClient.class))
		.withBean(HealthEndpoint.class, () -> mock(HealthEndpoint.class))
		.withBean(ConsulDiscoveryProperties.class, () -> mock(ConsulDiscoveryProperties.class))
		.withPropertyValues("spring.cloud.consul.discovery.heartbeat.enabled=true");

	@Test
	void heartbeatEnabled() {
		appContextRunner.run(this::assertThatHeartbeatConfigured);
	}

	@Test
	void heartbeatDisabled() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.heartbeat.enabled=false")
			.run(this::assertThatHeartbeatNotConfigured);
	}

	@Test
	void heartbeatEnabledPropertyNotSpecified() {
		new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ConsulHeartbeatAutoConfiguration.class))
			.withBean(ConsulClient.class, () -> mock(ConsulClient.class))
			.withBean(HealthEndpoint.class, () -> mock(HealthEndpoint.class))
			.run(this::assertThatHeartbeatNotConfigured);
	}

	@Test
	void heartbeatEnabledButConsulDisabled() {
		appContextRunner.withPropertyValues("spring.cloud.consul.enabled=false")
			.run(this::assertThatHeartbeatNotConfigured);
	}

	@Test
	void heartbeatEnabledButDiscoveryDisabled() {
		appContextRunner.withPropertyValues("spring.cloud.discovery.enabled=false")
			.run(this::assertThatHeartbeatNotConfigured);
	}

	private void assertThatHeartbeatNotConfigured(AssertableApplicationContext context) {
		assertThat(context).hasNotFailed()
			.doesNotHaveBean(HeartbeatProperties.class)
			.doesNotHaveBean(TtlScheduler.class)
			.doesNotHaveBean(ApplicationStatusProvider.class);
	}

	private void assertThatHeartbeatConfigured(AssertableApplicationContext context) {
		assertThat(context).hasNotFailed()
			.hasSingleBean(HeartbeatProperties.class)
			.hasSingleBean(TtlScheduler.class)
			.hasSingleBean(ApplicationStatusProvider.class);
	}

	@Test
	void heartbeatEnabledAndActuatorNotOnClasspath() {
		new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ConsulHeartbeatAutoConfiguration.class))
			.withBean(ConsulClient.class, () -> mock(ConsulClient.class))
			.withBean(ConsulDiscoveryProperties.class, () -> mock(ConsulDiscoveryProperties.class))
			.withPropertyValues("spring.cloud.consul.discovery.heartbeat.enabled=true")
			.withClassLoader(new FilteredClassLoader(HealthEndpoint.class))
			.run(this::assertThatHeartbeatConfiguredWithoutAppStatusProvider);
	}

	@Test
	void heartbeatEnabledAndActuatorOnClasspathButNoHealthEndpointBeanRegistered() {
		new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ConsulHeartbeatAutoConfiguration.class))
			.withBean(ConsulClient.class, () -> mock(ConsulClient.class))
			.withBean(ConsulDiscoveryProperties.class, () -> mock(ConsulDiscoveryProperties.class))
			.withPropertyValues("spring.cloud.consul.discovery.heartbeat.enabled=true")
			.run(this::assertThatHeartbeatConfiguredWithoutAppStatusProvider);
	}

	@Test
	void heartbeatEnabledButUseActuatorHealthPropertySetToFalse() {
		appContextRunner.withPropertyValues("spring.cloud.consul.discovery.heartbeat.use-actuator-health=false")
			.run(this::assertThatHeartbeatConfiguredWithoutAppStatusProvider);
	}

	private void assertThatHeartbeatConfiguredWithoutAppStatusProvider(AssertableApplicationContext context) {
		assertThat(context).hasNotFailed()
			.hasSingleBean(HeartbeatProperties.class)
			.hasSingleBean(TtlScheduler.class)
			.doesNotHaveBean(ApplicationStatusProvider.class);
	}

	@Test
	void customHeartbeatPropertiesRespected() {
		HeartbeatProperties customHeartbeatProps = mock(HeartbeatProperties.class);
		appContextRunner.withBean(HeartbeatProperties.class, () -> customHeartbeatProps)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(HeartbeatProperties.class)
				.getBean(HeartbeatProperties.class)
				.isSameAs(customHeartbeatProps));
	}

	@Test
	void customTtlSchedulerRespected() {
		TtlScheduler customTtlScheduler = mock(TtlScheduler.class);
		appContextRunner.withBean(TtlScheduler.class, () -> customTtlScheduler)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(TtlScheduler.class)
				.getBean(TtlScheduler.class)
				.isSameAs(customTtlScheduler));

	}

	@Test
	void customApplicationStatusProviderRespected() {
		ApplicationStatusProvider customAppStatusProvider = mock(ApplicationStatusProvider.class);
		appContextRunner.withBean(ApplicationStatusProvider.class, () -> customAppStatusProvider)
			.run(context -> assertThat(context).hasNotFailed()
				.hasSingleBean(ApplicationStatusProvider.class)
				.getBean(ApplicationStatusProvider.class)
				.isSameAs(customAppStatusProvider));

	}

}
