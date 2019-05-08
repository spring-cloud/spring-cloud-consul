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

import java.util.Arrays;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * @author Piotr Wielgolaski
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK,
		classes = ConsulDiscoveryClientDefaultQueryTagTests.TestConfig.class,
		properties = { "spring.application.name=consulServiceDefaultTag",
				"spring.cloud.consul.discovery.catalogServicesWatch.enabled=false",
				"spring.cloud.consul.discovery.defaultQueryTag=intg" })
@DirtiesContext
public class ConsulDiscoveryClientDefaultQueryTagTests {

	public static final String NAME = "consulServiceDefaultTag";

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private ConsulClient consulClient;

	private NewService intgService = serviceForEnvironment("intg", 9081);

	private NewService uatService = serviceForEnvironment("uat", 9080);

	@Before
	public void setUp() throws Exception {
		this.consulClient.agentServiceRegister(this.intgService);
		this.consulClient.agentServiceRegister(this.uatService);
	}

	@After
	public void tearDown() throws Exception {
		this.consulClient.agentServiceDeregister(this.intgService.getId());
		this.consulClient.agentServiceDeregister(this.uatService.getId());
	}

	@Test
	public void shouldReturnOnlyIntgInstance() {
		List<ServiceInstance> instances = this.discoveryClient.getInstances(NAME);
		assertThat(instances).as("instances was wrong size").hasSize(1);
		assertThat(instances.get(0).getMetadata()).as("instance is not intg")
				.containsEntry("intg", "intg");
	}

	private NewService serviceForEnvironment(String env, int port) {
		NewService service = new NewService();
		service.setAddress("localhost");
		service.setId(NAME + env);
		service.setName(NAME);
		service.setPort(port);
		service.setTags(Arrays.asList(env));
		return service;
	}

	@Configuration
	@EnableAutoConfiguration
	@Import({ ConsulDiscoveryClientConfiguration.class })
	protected static class TestConfig {

	}

}
