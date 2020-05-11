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

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Varnson Fan
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulServerListConsistencyModeDefaultTests.TestConfig.class,
		properties = { "spring.application.name=testConsulServerListConsistencyMode",
				"spring.cloud.consul.discovery.preferIpAddress=true" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulServerListConsistencyModeDefaultTests {

	@Autowired
	private ConsulClient consulClient;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Test
	public void serverListWorksWithConsistencyMode() {
		ConsulServerList consulServerList = new ConsulServerList(this.consulClient,
				this.properties);

		assertThat(consulServerList.getProperties().getConsistencyMode()
				.equals(ConsistencyMode.DEFAULT)).as("ConsistencyMode is default")
						.isTrue();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	public static class TestConfig {

	}

}
