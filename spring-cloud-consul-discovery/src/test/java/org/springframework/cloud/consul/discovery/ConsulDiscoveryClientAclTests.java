/*
 * Copyright 2013-2016 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ConsulDiscoveryClientAclTests.MyTestConfig.class)
@WebIntegrationTest(value = {"spring.application.name=testConsulDiscoveryAcl",
		"spring.cloud.consul.discovery.preferIpAddress=true",
		"consul.token=2d2e6b3b-1c82-40ab-8171-54609d8ad304"}, randomPort = true)
public class ConsulDiscoveryClientAclTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Test
	public void getInstancesForThisServiceWorks() {
		List<ServiceInstance> instances = discoveryClient.getInstances("testConsulDiscoveryAcl");
		assertNotNull("instances was null", instances);
		assertFalse("instances was empty", instances.isEmpty());
	}


	@Test
	public void getInstancesForSecondServiceWorks() throws Exception {

		new SpringApplicationBuilder(MySecondService.class)
				.run("--spring.application.name=testSecondServiceAcl",
						"--server.port=0",
						"--spring.cloud.consul.discovery.preferIpAddress=true",
						"--consul.token=2d2e6b3b-1c82-40ab-8171-54609d8ad304");

		List<ServiceInstance> instances = discoveryClient.getInstances("testSecondServiceAcl");
		assertNotNull("second service instances was null", instances);
		assertFalse("second service instances was empty", instances.isEmpty());
	}

	@Configuration
	@EnableDiscoveryClient
	@EnableAutoConfiguration
	@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
	public static class MyTestConfig {

	}

	@Configuration
	@EnableDiscoveryClient
	@EnableAutoConfiguration
	@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
	public static class MySecondService {

	}
}
