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

import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TtlSchedulerTests.TtlSchedulerTestConfig.class,
		properties = { "spring.application.name=ttlScheduler",
				"spring.cloud.consul.discovery.instance-id=ttlScheduler-id",
				"spring.cloud.consul.discovery.heartbeat.enabled=true",
				"spring.cloud.consul.discovery.heartbeat.ttlValue=2",
				"management.server.port=0" },
		webEnvironment = RANDOM_PORT)
public class TtlSchedulerTests {

	@Autowired
	private ConsulClient consul;

	@Test
	public void should_send_a_check_before_ttl_for_all_services()
			throws InterruptedException {
		Thread.sleep(2100); // Wait for TTL to expired (TTL is set to 2 seconds)

		Check serviceCheck = getCheckForService("ttlScheduler");
		assertThat(serviceCheck).isNotNull();
		assertThat(serviceCheck.getStatus()).isEqualTo(PASSING)
				.as("Service check is in wrong state");
		Check serviceManagementCheck = getCheckForService("ttlScheduler-management");
		assertThat(serviceManagementCheck).isNotNull();
		assertThat(serviceManagementCheck.getStatus()).isEqualTo(PASSING)
				.as("Service management check is in wrong state");
	}

	private Check getCheckForService(String serviceId) {
		Response<List<Check>> checkResponse = this.consul
				.getHealthChecksForService(serviceId, QueryParams.DEFAULT);
		if (checkResponse.getValue().size() > 0) {
			return checkResponse.getValue().get(0);
		}
		return null;
	}

	@Configuration
	@EnableAutoConfiguration
	@Import({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulDiscoveryClientConfiguration.class,
			ConsulHeartbeatAutoConfiguration.class })
	public static class TtlSchedulerTestConfig {

	}

}
