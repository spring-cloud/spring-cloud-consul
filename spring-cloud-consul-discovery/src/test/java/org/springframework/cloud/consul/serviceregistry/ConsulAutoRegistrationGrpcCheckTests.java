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

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit tests for the gRPC health check created by {@link ConsulAutoRegistration}.
 *
 * @author Asish Kumar
 */
class ConsulAutoRegistrationGrpcCheckTests {

	private ConsulDiscoveryProperties properties;

	private HeartbeatProperties heartbeatProperties;

	@BeforeEach
	void setUp() {
		this.properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		this.properties.setHostname("myhost");
		this.properties.setHealthCheckGrpcPort(9090);
		this.heartbeatProperties = new HeartbeatProperties();
	}

	@Test
	void grpcCheckChecksWholeServerByDefault() {
		NewService.Check check = ConsulAutoRegistration.createGrpcCheck(this.properties);

		assertThat(check.getGrpc()).isEqualTo("myhost:9090");
		assertThat(check.getGrpcUseTLS()).isFalse();
		assertThat(check.getInterval()).isEqualTo("10s");
		assertThat(check.getHttp()).isNull();
		assertThat(check.getDeregisterCriticalServiceAfter()).isNull();
	}

	@Test
	void grpcCheckChecksConfiguredService() {
		this.properties.setHealthCheckGrpcService("my.package.MyService");

		NewService.Check check = ConsulAutoRegistration.createGrpcCheck(this.properties);

		assertThat(check.getGrpc()).isEqualTo("myhost:9090/my.package.MyService");
	}

	@Test
	void grpcCheckEnclosesIpv6AddressInBrackets() {
		this.properties.setIpAddress("2001:db8::1");
		this.properties.setPreferIpAddress(true);

		NewService.Check check = ConsulAutoRegistration.createGrpcCheck(this.properties);

		assertThat(check.getGrpc()).isEqualTo("[2001:db8::1]:9090");
	}

	@Test
	void grpcCheckRejectsInvalidPort() {
		this.properties.setHealthCheckGrpcPort(0);

		assertThatIllegalArgumentException().isThrownBy(() -> ConsulAutoRegistration.createGrpcCheck(this.properties));
	}

	@Test
	void serviceCheckIsGrpcCheck() {
		NewService service = service();

		ConsulAutoRegistration.setCheck(service, new AutoServiceRegistrationProperties(), this.properties,
				new StaticApplicationContext(), this.heartbeatProperties);

		assertThat(service.getCheck().getGrpc()).isEqualTo("myhost:9090");
		assertThat(service.getCheck().getHttp()).isNull();
	}

	@Test
	void ttlCheckTakesPrecedenceOverGrpcCheck() {
		this.heartbeatProperties.setEnabled(true);
		NewService service = service();

		ConsulAutoRegistration.setCheck(service, new AutoServiceRegistrationProperties(), this.properties,
				new StaticApplicationContext(), this.heartbeatProperties);

		assertThat(service.getCheck().getTtl()).isEqualTo("30s");
		assertThat(service.getCheck().getGrpc()).isNull();
	}

	@Test
	void noCheckWhenHealthCheckRegistrationDisabled() {
		this.properties.setRegisterHealthCheck(false);
		NewService service = service();

		ConsulAutoRegistration.setCheck(service, new AutoServiceRegistrationProperties(), this.properties,
				new StaticApplicationContext(), this.heartbeatProperties);

		assertThat(service.getCheck()).isNull();
	}

	@Test
	void managementServiceKeepsHttpCheck() {
		this.properties.setServiceName("myservice");
		this.properties.setInstanceId("myservice-0");
		this.properties.setManagementPort(4452);

		ConsulAutoRegistration registration = ConsulAutoRegistration.managementRegistration(
				new AutoServiceRegistrationProperties(), this.properties, new StaticApplicationContext(),
				Collections.emptyList(), this.heartbeatProperties);

		NewService.Check check = registration.getService().getCheck();
		assertThat(check.getHttp()).isEqualTo("http://myhost:4452/actuator/health");
		assertThat(check.getGrpc()).isNull();
	}

	private static NewService service() {
		NewService service = new NewService();
		service.setPort(8080);
		return service;
	}

}
