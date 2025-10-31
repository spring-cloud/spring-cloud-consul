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

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.discovery.TtlScheduler.ConsulHeartbeatTask;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.serviceregistry.ApplicationStatusProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

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
 * @author Chris Bono
 */
public class ConsulHeartbeatTaskTests {

	private String serviceId = "service-A";

	private HeartbeatProperties heartbeatProperties;

	private ConsulDiscoveryProperties discoveryProperties;

	private ConsulClient consulClient;

	private ObjectProvider<ApplicationStatusProvider> applicationStatusProviders;

	private ApplicationStatusProvider applicationStatusProvider;

	@BeforeEach
	public void setUp() {
		this.heartbeatProperties = new HeartbeatProperties();
		this.discoveryProperties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		this.consulClient = mock(ConsulClient.class);
		setupApplicationStatusProvider(Check.CheckStatus.PASSING);
	}

	private void setupApplicationStatusProvider(Check.CheckStatus desiredCheckStatus) {
		applicationStatusProvider = () -> desiredCheckStatus;
		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory(
				Collections.singletonMap("applicationStatusProvider", applicationStatusProvider));
		this.applicationStatusProviders = beanFactory.getBeanProvider(ApplicationStatusProvider.class);
	}

	@Test
	public void enableReRegistrationForAgentCheckPass() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReregistrationPredicate.DEFAULT, applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.PASSING);
		heartbeatProperties.setReregisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		given(consulClient.agentCheckPass("service:" + serviceId, null, null))
			.willThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
					"CheckID \"service:service-A\" does not have associated TTL".getBytes(StandardCharsets.UTF_8),
					StandardCharsets.UTF_8));
		consulHeartbeatTask.run();
		ArgumentCaptor<NewService> serviceCaptor = ArgumentCaptor.forClass(NewService.class);
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(consulClient, atLeastOnce()).agentServiceRegister(tokenCaptor.capture(), serviceCaptor.capture());
		assertThat(serviceCaptor.getValue()).isSameAs(service);
	}

	@Test
	public void enableReRegistrationForAgentCheckWarn() {
		setupApplicationStatusProvider(Check.CheckStatus.WARNING);
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReregistrationPredicate.DEFAULT, applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.WARNING);
		heartbeatProperties.setReregisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		given(consulClient.agentCheckWarn("service:" + serviceId, null, null))
			.willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
					"CheckID \"service:service-A\" does not have associated TTL".getBytes(StandardCharsets.UTF_8),
					StandardCharsets.UTF_8));
		consulHeartbeatTask.run();
		ArgumentCaptor<NewService> serviceCaptor = ArgumentCaptor.forClass(NewService.class);
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(consulClient, atLeastOnce()).agentServiceRegister(tokenCaptor.capture(), serviceCaptor.capture());
		assertThat(serviceCaptor.getValue()).isSameAs(service);
	}

	@Test
	public void enableReRegistrationForAgentCheckFail() {
		setupApplicationStatusProvider(Check.CheckStatus.CRITICAL);
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReregistrationPredicate.DEFAULT, applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.CRITICAL);
		heartbeatProperties.setReregisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		given(consulClient.agentCheckFail("service:" + serviceId, null, null))
			.willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
					"CheckID \"service:service-A\" does not have associated TTL".getBytes(StandardCharsets.UTF_8),
					StandardCharsets.UTF_8));
		consulHeartbeatTask.run();
		ArgumentCaptor<NewService> serviceCaptor = ArgumentCaptor.forClass(NewService.class);
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(consulClient, atLeastOnce()).agentServiceRegister(tokenCaptor.capture(), serviceCaptor.capture());
		assertThat(serviceCaptor.getValue()).isSameAs(service);
	}

	@Test
	public void eligibleForReRegistrationWith404() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReregistrationPredicate.DEFAULT, applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.PASSING);
		heartbeatProperties.setReregisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		HttpClientErrorException operationException = new HttpClientErrorException(HttpStatus.NOT_FOUND,
				"Internal Server Error",
				"CheckID \"service:service-A\" does not have associated TTL".getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8);
		given(consulClient.agentCheckPass("service:" + serviceId, null, null)).willThrow(operationException);
		consulHeartbeatTask.run();
	}

	@Test
	public void notEligibleForReRegistration() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReregistrationPredicate.DEFAULT, applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.PASSING);
		heartbeatProperties.setReregisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		HttpClientErrorException operationException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,
				"Internal Server Error",
				"CheckID \"service:service-A\" does not have associated TTL".getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8);
		given(consulClient.agentCheckPass("service:" + serviceId, null, null)).willThrow(operationException);
		assertThatThrownBy(consulHeartbeatTask::run).isSameAs(operationException);
	}

	@Test
	public void enableReRegistrationWithCustomPredicate() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				e -> e.getMessage().endsWith("does not have associated TTL"), applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.PASSING);
		heartbeatProperties.setReregisterServiceOnFailure(true);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		given(consulClient.agentCheckPass("service:" + serviceId, null, null))
			.willThrow(new HttpClientErrorException("CheckID \"service:service-A\" does not have associated TTL",
					HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));
		consulHeartbeatTask.run();
		ArgumentCaptor<NewService> serviceCaptor = ArgumentCaptor.forClass(NewService.class);
		ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
		verify(consulClient, atLeastOnce()).agentServiceRegister(tokenCaptor.capture(), serviceCaptor.capture());
		assertThat(serviceCaptor.getValue()).isSameAs(service);
	}

	@Test
	public void disableReRegistration() {
		TtlScheduler ttlScheduler = new TtlScheduler(heartbeatProperties, discoveryProperties, consulClient,
				ReregistrationPredicate.DEFAULT, applicationStatusProviders);
		ConsulHeartbeatTask consulHeartbeatTask = new ConsulHeartbeatTask(serviceId, ttlScheduler,
				() -> Check.CheckStatus.PASSING);
		heartbeatProperties.setReregisterServiceOnFailure(false);
		NewService service = new NewService();
		service.setId(serviceId);
		ttlScheduler.add(service);
		HttpServerErrorException operationException = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
				"Internal Server Error",
				"CheckID \"service:service-A\" does not have associated TTL".getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8);
		given(consulClient.agentCheckPass("service:" + serviceId, null, null)).willThrow(operationException);
		assertThatThrownBy(consulHeartbeatTask::run).isSameAs(operationException);
	}

}
