/*
 * Copyright 2012-present the original author or authors.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author Ryan Baxter
 */

@Testcontainers
public class ConsulConfigServerBootstrapperIT {

	public static final DockerImageName MOCKSERVER_IMAGE = DockerImageName.parse("mockserver/mockserver")
		.withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());

	@Container
	static ConsulContainer consul = ConsulTestcontainers.createConsulContainer("1.10");

	@Container
	static MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);

	private ConfigurableApplicationContext context;

	@BeforeEach
	void before() {
		ConsulProperties consulProperties = new ConsulProperties();
		consulProperties.setHost(consul.getHost());
		consulProperties.setPort(consul.getMappedPort(ConsulTestcontainers.DEFAULT_PORT));
		ConsulClient client = ConsulAutoConfiguration.createConsulClient(consulProperties,
				ConsulAutoConfiguration.createConsulRawClientBuilder());
		NewService newService = new NewService();
		newService.setId("consul-configserver");
		newService.setName("consul-configserver");
		newService.setAddress(mockServer.getHost());
		newService.setPort(mockServer.getServerPort());
		client.agentServiceRegister(newService);

	}

	@AfterEach
	void after() {
		this.context.close();
	}

	@Test
	public void contextLoads() throws JsonProcessingException {
		Environment environment = new Environment("test", "default");
		Map<String, Object> properties = new HashMap<>();
		properties.put("hello", "world");
		PropertySource p = new PropertySource("p1", properties);
		environment.add(p);
		ObjectMapper objectMapper = new ObjectMapper();
		try (MockServerClient mockServerClient = new MockServerClient(mockServer.getHost(),
				mockServer.getMappedPort(MockServerContainer.PORT))) {
			mockServerClient.when(request().withPath("/application/default"))
				.respond(response().withBody(objectMapper.writeValueAsString(environment))
					.withHeader("content-type", "application/json"));
			this.context = setup().run();
			assertThat(this.context.getEnvironment().getProperty("hello")).isEqualTo("world");
		}

	}

	SpringApplicationBuilder setup(String... env) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(TestConfig.class)
			.properties(addDefaultEnv(env));
		return builder;
	}

	private String[] addDefaultEnv(String[] env) {
		Set<String> set = new LinkedHashSet<>();
		if (env != null && env.length > 0) {
			set.addAll(Arrays.asList(env));
		}
		set.add("spring.config.import=classpath:bootstrapper.yaml");
		set.add("spring.cloud.config.enabled=true");
		set.add("spring.cloud.service-registry.auto-registration.enabled=false");
		set.add(ConsulProperties.PREFIX + ".host=" + consul.getHost());
		set.add(ConsulProperties.PREFIX + ".port=" + consul.getMappedPort(ConsulTestcontainers.DEFAULT_PORT));
		return set.toArray(new String[0]);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestConfig {

	}

}
