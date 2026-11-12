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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ConsulServiceRegistryRegistrationTests {

	@Test
	void publishesRegistrationCompletedEventAfterSuccessfulRegistration() {
		ConsulClient consulClient = mock(ConsulClient.class);
		ConsulDiscoveryProperties discoveryProperties = mock(ConsulDiscoveryProperties.class);
		HeartbeatProperties heartbeatProperties = new HeartbeatProperties();
		TtlScheduler ttlScheduler = mock(TtlScheduler.class);
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

		ConsulServiceRegistry serviceRegistry = new ConsulServiceRegistry(consulClient, discoveryProperties,
				ttlScheduler, heartbeatProperties);
		serviceRegistry.setApplicationEventPublisher(publisher);

		NewService service = new NewService();
		service.setId("test-service");
		service.setName("test-service");

		ConsulRegistration registration = new ConsulRegistration(service, discoveryProperties);

		serviceRegistry.register(registration);

		ArgumentCaptor<ConsulRegistrationCompleted> eventCaptor = ArgumentCaptor
			.forClass(ConsulRegistrationCompleted.class);

		verify(publisher).publishEvent(eventCaptor.capture());

		assertThat(eventCaptor.getValue().getRegistration()).isSameAs(registration);
	}

	@Test
	void schedulesHeartbeatWhenInitialRegistrationFailsAndFailFastIsDisabled() {
		ConsulClient consulClient = mock(ConsulClient.class);
		ConsulDiscoveryProperties discoveryProperties = mock(ConsulDiscoveryProperties.class);
		HeartbeatProperties heartbeatProperties = new HeartbeatProperties();
		heartbeatProperties.setEnabled(true);
		TtlScheduler ttlScheduler = mock(TtlScheduler.class);
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

		given(discoveryProperties.isFailFast()).willReturn(false);

		ConsulServiceRegistry serviceRegistry = new ConsulServiceRegistry(consulClient, discoveryProperties,
				ttlScheduler, heartbeatProperties);
		serviceRegistry.setApplicationEventPublisher(publisher);

		NewService service = new NewService();
		service.setId("test-service");
		service.setName("test-service");

		NewService.Check check = new NewService.Check();
		check.setTtl("10s");
		service.setCheck(check);

		ConsulRegistration registration = new ConsulRegistration(service, discoveryProperties);

		given(consulClient.agentServiceRegister(discoveryProperties.getAclToken(), service))
			.willThrow(new IllegalStateException("Consul is unavailable"));

		assertThatCode(() -> serviceRegistry.register(registration)).doesNotThrowAnyException();

		verify(ttlScheduler).add(service);
		verify(publisher, never()).publishEvent(any(ConsulRegistrationCompleted.class));
	}

	@Test
	void doesNotScheduleHeartbeatWhenInitialRegistrationFailsAndFailFastIsEnabled() {
		ConsulClient consulClient = mock(ConsulClient.class);
		ConsulDiscoveryProperties discoveryProperties = mock(ConsulDiscoveryProperties.class);
		HeartbeatProperties heartbeatProperties = new HeartbeatProperties();
		heartbeatProperties.setEnabled(true);
		TtlScheduler ttlScheduler = mock(TtlScheduler.class);
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

		given(discoveryProperties.isFailFast()).willReturn(true);

		ConsulServiceRegistry serviceRegistry = new ConsulServiceRegistry(consulClient, discoveryProperties,
				ttlScheduler, heartbeatProperties);
		serviceRegistry.setApplicationEventPublisher(publisher);

		NewService service = new NewService();
		service.setId("test-service");
		service.setName("test-service");

		NewService.Check check = new NewService.Check();
		check.setTtl("10s");
		service.setCheck(check);

		ConsulRegistration registration = new ConsulRegistration(service, discoveryProperties);

		given(consulClient.agentServiceRegister(discoveryProperties.getAclToken(), service))
			.willThrow(new IllegalStateException("Consul is unavailable"));

		assertThatCode(() -> serviceRegistry.register(registration)).isInstanceOf(IllegalStateException.class)
			.hasMessage("Consul is unavailable");

		verify(ttlScheduler, never()).add(service);
		verify(publisher, never()).publishEvent(any(ConsulRegistrationCompleted.class));
	}

}
