/*
 * Copyright 2013-2017 the original author or authors.
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

import java.lang.reflect.Field;
import java.util.Map;

import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexey Savchuk
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulServiceRegistryCheckTtlTests.TestConfig.class,
		properties = {
				"spring.application.name=myConsulServiceRegistryCheckTtlTestService-S",
				"spring.cloud.consul.discovery.heartbeat.enabled=true" },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConsulServiceRegistryCheckTtlTests {

	static final Logger logger = LoggerFactory.getLogger(ConsulServiceRegistryTests.class);

	public static GenericContainer consul = new GenericContainer("consul:1.7.1")
		.waitingFor(Wait.forHttp("/v1/status/leader"))
		.withExposedPorts(8500)
		.withCommand("agent", "-dev", "-server", "-bootstrap", "-client", "0.0.0.0", "-log-level", "trace");

	@LocalServerPort
	private int port;

	@BeforeClass
	public static void before() {
		consul.start();
		consul.followOutput(new Slf4jLogConsumer(logger).withSeparateOutputStreams());
		Integer mappedPort = consul.getMappedPort(8500);
		System.setProperty("spring.cloud.consul.port", String.valueOf(mappedPort));
	}

	@AfterClass
	public static void after() {
		System.clearProperty("spring.cloud.consul.port");
		consul.stop();
	}

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Autowired
	private ConsulServiceRegistry consulServiceRegistry;

	@Autowired
	private ConsulRegistration registration;

	@Autowired
	private TtlScheduler ttlScheduler;

	private ConsulRegistration createHttpRegistration() {
		NewService service = this.registration.getService();
		NewService.Check httpCheck = new NewService.Check();
		httpCheck.setHttp(
				String.format("%s://%s:%s%s", this.discoveryProperties.getScheme(),
						this.discoveryProperties.getHostname(), this.port,
						this.discoveryProperties.getHealthCheckPath()));
		httpCheck.setInterval(this.discoveryProperties.getHealthCheckInterval());
		NewService httpService = new NewService();
		httpService.setId(service.getId() + "-http");
		httpService.setName(service.getName() + "-http");
		httpService.setCheck(httpCheck);
		return new ConsulRegistration(httpService, this.discoveryProperties);
	}

	@Test
	public void contextLoads() throws NoSuchFieldException, IllegalAccessException {
		ConsulRegistration httpRegistration = createHttpRegistration();
		try {
			this.consulServiceRegistry.register(httpRegistration);
			Field serviceHeartbeatsField = TtlScheduler.class
					.getDeclaredField("serviceHeartbeats");
			serviceHeartbeatsField.setAccessible(true);
			Map serviceHeartbeats = (Map) serviceHeartbeatsField.get(this.ttlScheduler);
			assertThat(serviceHeartbeats.keySet()
					.contains(this.registration.getInstanceId())).as(
							"Service with heartbeat check not registered in TTL scheduler")
							.isTrue();
			assertThat(
					serviceHeartbeats.keySet().contains(httpRegistration.getInstanceId()))
							.as("Service with HTTP check registered in TTL scheduler")
							.isFalse();
		}
		finally {
			this.consulServiceRegistry.deregister(httpRegistration);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class,
			ConsulHeartbeatAutoConfiguration.class })
	protected static class TestConfig {

	}

}
