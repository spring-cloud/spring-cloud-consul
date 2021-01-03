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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler.ConsulHeartbeatTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for ConsulHeartbeatTask
 *
 * @author Toshiaki Maki
 */
public class ConsulHeartbeatTaskTests {

	String serviceId = "service-A";

	HeartbeatProperties heartbeatProperties;

	ConsulDiscoveryProperties discoveryProperties;

	ConsulClient consulClient;

	@Before
	public void setUp() {
		this.heartbeatProperties = new HeartbeatProperties();
		this.discoveryProperties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		this.consulClient = mock(ConsulClient.class);
	}

	@Test
	public void enableReRegistration() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReRegistrationPredicate.DEFAULT);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler);
		heartbeatProperties.setReRegisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		given(consulClient.agentCheckPass("service:" + serviceId)).willThrow(new OperationException(500,
				"Internal Server Error", "CheckID \"service:service-A\" does not have associated TTL"));
		consulHeartbeatTask.run();
		ArgumentCaptor<NewService> serviceCaptor = ArgumentCaptor.forClass(NewService.class);
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(consulClient, atLeastOnce()).agentServiceRegister(serviceCaptor.capture(), tokenCaptor.capture());
		assertThat(serviceCaptor.getValue()).isSameAs(service);
	}

	@Test
	public void notEligibleForReRegistration() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReRegistrationPredicate.DEFAULT);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler);
		heartbeatProperties.setReRegisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		OperationException operationException = new OperationException(400, "Internal Server Error",
				"CheckID \"service:service-A\" does not have associated TTL");
		given(consulClient.agentCheckPass("service:" + serviceId)).willThrow(operationException);
		assertThatThrownBy(consulHeartbeatTask::run).isSameAs(operationException);
	}

	@Test
	public void enableReRegistrationWithCustomPredicate() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				e -> e.getStatusContent().endsWith("does not have associated TTL"));
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler);
		heartbeatProperties.setReRegisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		given(consulClient.agentCheckPass("service:" + serviceId)).willThrow(new OperationException(400,
				"Internal Server Error", "CheckID \"service:service-A\" does not have associated TTL"));
		consulHeartbeatTask.run();
		ArgumentCaptor<NewService> serviceCaptor = ArgumentCaptor.forClass(NewService.class);
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(consulClient, atLeastOnce()).agentServiceRegister(serviceCaptor.capture(), tokenCaptor.capture());
		assertThat(serviceCaptor.getValue()).isSameAs(service);
	}

	@Test
	public void disableReRegistration() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReRegistrationPredicate.DEFAULT);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler);
		heartbeatProperties.setReRegisterServiceOnFailure(false);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		OperationException operationException = new OperationException(500, "Internal Server Error",
				"CheckID \"service:service-A\" does not have associated TTL");
		given(consulClient.agentCheckPass("service:" + serviceId)).willThrow(operationException);
		assertThatThrownBy(consulHeartbeatTask::run).isSameAs(operationException);
	}

}
