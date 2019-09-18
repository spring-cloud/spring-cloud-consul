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

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TtlSchedulerRemoveTests.TtlSchedulerRemoveTestConfig.class,
		properties = { "spring.application.name=ttlSchedulerRemove",
				"spring.cloud.consul.discovery.instance-id=ttlSchedulerRemove-id",
				"spring.cloud.consul.discovery.heartbeat.enabled=true",
				"spring.cloud.consul.discovery.heartbeat.ttlValue=2" },
		webEnvironment = RANDOM_PORT)
public class TtlSchedulerRemoveTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private TtlScheduler ttlScheduler;

	@Test
	public void should_not_send_check_if_service_removed() throws InterruptedException {
		Thread.sleep(1000); // wait for Ttlscheduler to send a check to consul.
		Check serviceCheck = getCheckForService("ttlSchedulerRemove");
		assertThat(serviceCheck.getStatus()).as("Service check is in wrong state")
				.isEqualTo(PASSING);

		// Remove service from TtlScheduler and wait for TTL to expired.
		this.ttlScheduler.remove("ttlSchedulerRemove-id");
		Thread.sleep(2100);
		serviceCheck = getCheckForService("ttlSchedulerRemove");
		assertThat(serviceCheck.getStatus()).as("Service check is in wrong state")
				.isEqualTo(CRITICAL);
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
	public static class TtlSchedulerRemoveTestConfig {

	}

}
