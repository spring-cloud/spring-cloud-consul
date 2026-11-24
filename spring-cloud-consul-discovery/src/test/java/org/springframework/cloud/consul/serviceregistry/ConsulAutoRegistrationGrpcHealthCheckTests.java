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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Asish Kumar
 */
@SpringBootTest(classes = ConsulAutoRegistrationGrpcHealthCheckTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-GrpcHealthCheck",
				"spring.cloud.consul.discovery.instance-id=myTestService-GrpcHealthCheck-0",
				"spring.cloud.consul.discovery.health-check-grpc-port=9090",
				"spring.cloud.consul.discovery.health-check-grpc-service=my.package.MyService",
				"spring.cloud.consul.discovery.health-check-grpc-use-tls=true",
				"spring.cloud.consul.discovery.health-check-interval=15s",
				"spring.cloud.consul.discovery.health-check-timeout=5s",
				"spring.cloud.consul.discovery.health-check-critical-timeout=30m",
				"spring.cloud.consul.discovery.health-check-tls-skip-verify=true" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoRegistrationGrpcHealthCheckTests {

	@Autowired
	private ConsulAutoRegistration registration;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Autowired
	private ConsulClient consul;

	@Test
	public void grpcCheckRegistered() {
		NewService.Check check = this.registration.getService().getCheck();
		assertThat(check).as("check was null").isNotNull();
		assertThat(check.getGrpc()).isEqualTo(this.properties.getHostname() + ":9090/my.package.MyService");
		assertThat(check.getGrpcUseTLS()).isTrue();
		assertThat(check.getHttp()).isNull();
		assertThat(check.getTtl()).isNull();
		assertThat(check.getHeader()).isNull();
		assertThat(check.getInterval()).isEqualTo("15s");
		assertThat(check.getTimeout()).isEqualTo("5s");
		assertThat(check.getDeregisterCriticalServiceAfter()).isEqualTo("30m");
		assertThat(check.getTlsSkipVerify()).isTrue();

		List<Check> checks = this.consul.getHealthChecksForService("myTestService-GrpcHealthCheck").getBody();
		assertThat(checks).extracting(Check::getServiceId).containsExactly("myTestService-GrpcHealthCheck-0");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
