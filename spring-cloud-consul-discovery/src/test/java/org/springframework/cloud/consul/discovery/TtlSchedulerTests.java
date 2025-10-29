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

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.serviceregistry.ApplicationStatusProvider;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.consul.model.http.health.Check.CheckStatus.CRITICAL;
import static org.springframework.cloud.consul.model.http.health.Check.CheckStatus.PASSING;
import static org.springframework.cloud.consul.model.http.health.Check.CheckStatus.WARNING;

/**
 * Unit tests for {@link TtlScheduler}.
 *
 * @author Stéphane Leroy
 * @author Chris Bono
 */
@ExtendWith(MockitoExtension.class)
class TtlSchedulerTests {

	@Mock
	private HeartbeatProperties heartbeatProperties;

	@Mock
	private ConsulDiscoveryProperties discoveryProperties;

	@Mock
	private ConsulClient client;

	@Mock
	private ApplicationStatusProvider applicationStatusProvider;

	private TtlScheduler ttlScheduler;

	@BeforeEach
	void setup() {
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory(
				Collections.singletonMap("applicationStatusProvider", applicationStatusProvider));
		ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, client,
				ReregistrationPredicate.DEFAULT, beanFactory.getBeanProvider(ApplicationStatusProvider.class));
		when(heartbeatProperties.computeHeartbeatInterval()).thenReturn(Duration.ofMillis(2000));
	}

	@Test
	void agentCheckIsReportedProperAmountOfTimes() {
		String serviceId = addServiceToSchedulerWhenApplicationStatusIs(PASSING);
		// Wait for 5s and it should have run 3 times as the interval is 2s and it runs
		// immediately when added
		awaitFor(Duration.ofSeconds(5));
		verify(client, times(3)).agentCheckPass("service:" + serviceId, null, null);
	}

	@Test
	void agentCheckPassGetsCalledWhenApplicationStatusIsPassing() {
		String serviceId = addServiceToSchedulerWhenApplicationStatusIs(PASSING);
		verify(client).agentCheckPass("service:" + serviceId, null, null);
	}

	@Test
	void agentCheckWarnGetsCalledWhenApplicationStatusIsWarning() {
		String serviceId = addServiceToSchedulerWhenApplicationStatusIs(WARNING);
		verify(client).agentCheckWarn("service:" + serviceId, null, null);
	}

	@Test
	void agentCheckFailGetsCalledWhenApplicationStatusIsCritical() {
		String serviceId = addServiceToSchedulerWhenApplicationStatusIs(CRITICAL);
		verify(client).agentCheckFail("service:" + serviceId, null, null);
	}

	private String addServiceToSchedulerWhenApplicationStatusIs(Check.CheckStatus checkStatus) {
		String serviceId = "svc-" + checkStatus.name();
		when(applicationStatusProvider.currentStatus()).thenReturn(checkStatus);
		ttlScheduler.add(serviceId);
		// The scheduler runs immediately after the add(serviceId) - pause for 500ms
		awaitFor(Duration.ofMillis(500));
		verify(applicationStatusProvider).currentStatus();
		return serviceId;
	}

	private void awaitFor(Duration duration) {
		await().pollDelay(duration).until(() -> true);
	}

}
