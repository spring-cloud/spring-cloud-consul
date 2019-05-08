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

import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.netflix.client.config.DefaultClientConfigImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServer;
import org.springframework.cloud.consul.discovery.ConsulServerList;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Jin Zhang
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = ConsulAutoServiceRegistrationCustomizedInstanceGroupTests.TestConfig.class,
		properties = { "spring.application.name=myTestService-WithGroup",
				"spring.cloud.consul.discovery.instanceId=myTestService1-WithGroup",
				"spring.cloud.consul.discovery.instanceGroup=test" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationCustomizedInstanceGroupTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-WithGroup");
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort().intValue()).as("service port is 0").isNotEqualTo(0);
		assertThat(service.getId()).as("service id was wrong")
				.isEqualTo("myTestService1-WithGroup");
		assertThat(service.getTags().contains("group=test")).as("service group was wrong")
				.isTrue();

		ConsulServerList serverList = new ConsulServerList(this.consul, this.properties);
		DefaultClientConfigImpl config = new DefaultClientConfigImpl();
		config.setClientName("myTestService-WithGroup");
		serverList.initWithNiwsConfig(config);

		List<ConsulServer> servers = serverList.getInitialListOfServers();
		assertThat(servers.size()).as("servers was wrong size").isEqualTo(1);
		assertThat(servers.get(0).getMetaInfo().getServerGroup())
				.as("service group was wrong").isEqualTo("test");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {

	}

}
