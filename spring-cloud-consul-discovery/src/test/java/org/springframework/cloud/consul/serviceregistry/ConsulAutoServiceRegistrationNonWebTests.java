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

package org.springframework.cloud.consul.serviceregistry;

import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationNonWebTests.TestConfig.class,
		properties = { "spring.application.name=consulNonWebTest", "server.port=32111" },
		webEnvironment = NONE)
public class ConsulAutoServiceRegistrationNonWebTests {

	@Autowired
	private ConsulClient consul;

	@Autowired(required = false)
	private ConsulAutoServiceRegistration autoServiceRegistration;

	@Test
	public void contextLoads() {
		assertThat(this.autoServiceRegistration)
				.as("ConsulAutoServiceRegistration was created").isNotNull();

		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("consulNonWebTest");
		assertThat(service).as("service was registered").isNull(); // no port to listen,
																	// hence no
		// registration
	}

	@EnableDiscoveryClient
	@Configuration
	@EnableAutoConfiguration
	public static class TestConfig {

	}

}
