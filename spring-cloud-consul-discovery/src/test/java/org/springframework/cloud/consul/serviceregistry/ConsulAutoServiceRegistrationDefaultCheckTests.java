/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationCustomizedPropsTests.TestPropsConfig;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 * @deprecated remove in Edgware
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestPropsConfig.class,
	properties = { "spring.application.name=myTestServiceDefaultChecks",
			"spring.cloud.consul.discovery.instanceId=myTestServiceDefaultChecks",
			"spring.cloud.consul.discovery.healthCheckCriticalTimeout=30m",
			"spring.cloud.consul.discovery.healthCheckInterval=19s",
			"spring.cloud.consul.discovery.healthCheckTimeout=12s",
	}, webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationDefaultCheckTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Test
	public void contextLoads() {
		assertThat(properties.getHealthCheckCriticalTimeout()).isEqualTo("30m");
		assertThat(properties.getHealthCheckInterval()).isEqualTo("19s");
		assertThat(properties.getHealthCheckTimeout()).isEqualTo("12s");

		// I'm unable to find a way to query consul to see the configuration of the health check
		// so for now, just sending the new healthCheckCriticalTimeout and having consul accept
		// it is going to have to suffice

		//final Response<List<com.ecwid.consul.v1.health.model.Check>> checksForService = consul.getHealthChecksForService("myTestServiceDefaultChecks", QueryParams.DEFAULT);
		//final List<com.ecwid.consul.v1.health.model.Check> checkList = checksForService.getValue();
		//final Response<Map<String, Check>> response2 = consul.getAgentChecks();
		//final Map<String, Check> checks = response2.getValue();
		//final Check check = checks.get("myTestServiceDefaultChecks");
		//Assertions.assertThat(check).isNotNull();
	}
}
