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
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Jin Zhang
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationCustomizedInstanceGroupTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-WithGroup",
				"spring.cloud.consul.discovery.instanceId=myTestService1-WithGroup",
				"spring.cloud.consul.discovery.instanceGroup=test" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoServiceRegistrationCustomizedInstanceGroupTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private LoadBalancerClient client;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-WithGroup");
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort().intValue()).as("service port is 0").isNotEqualTo(0);
		assertThat(service.getId()).as("service id was wrong").isEqualTo("myTestService1-WithGroup");
		assertThat(service.getMeta()).as("service group was wrong").containsEntry("group", "test");

		ServiceInstance instance = client.choose("myTestService-WithGroup");
		assertThat(instance).isNotNull();
		assertThat(instance.getMetadata()).containsEntry("group", "test");
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
