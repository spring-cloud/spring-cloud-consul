/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * @author Piotr Wielgolaski
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK,
				classes = ConsulDiscoveryClientDefaultQueryTagTests.TestConfig.class,
				properties = {
						"spring.application.name=consulServiceDefaultTag",
						"spring.cloud.consul.discovery.catalogServicesWatch.enabled=false",
						"spring.cloud.consul.discovery.defaultQueryTag=intg"})
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
		consulClient.agentServiceRegister(intgService);
		consulClient.agentServiceRegister(uatService);
	}

	@After
	public void tearDown() throws Exception {
		consulClient.agentServiceDeregister(intgService.getId());
		consulClient.agentServiceDeregister(uatService.getId());
	}

	@Test
	public void shouldReturnOnlyIntgInstance() {
		List<ServiceInstance> instances = discoveryClient.getInstances(NAME);
		assertThat("instances was wrong size", instances, hasSize(1));
		assertThat("instance is not intg", instances.get(0).getMetadata(), hasEntry("intg", "intg"));
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
