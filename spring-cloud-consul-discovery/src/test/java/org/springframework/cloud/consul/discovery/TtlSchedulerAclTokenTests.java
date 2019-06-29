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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TtlSchedulerAclTokenTests.TtlSchedulerTestConfig.class,
		properties = { "spring.application.name=ttlScheduler-acl-token",
				"spring.cloud.consul.discovery.acl-token=testtoken4ttl",
				"spring.cloud.consul.discovery.heartbeat.enabled=true",
				"spring.cloud.consul.discovery.heartbeat.ttlValue=2" },
		webEnvironment = RANDOM_PORT)
public class TtlSchedulerAclTokenTests {

	@Autowired
	private TtlScheduler ttlScheduler;

	@Test
	public void ttlSchedulerTokenCheck() throws InterruptedException {

		assertThat(ttlScheduler.getConsulDiscoveryProperties()).isNotNull()
				.as("properties is null");
		assertThat(ttlScheduler.getConsulDiscoveryProperties().getAclToken())
				.isEqualTo("testtoken4ttl").as("ttl acl token error");

	}

	@Configuration
	@EnableAutoConfiguration
	@Import({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulDiscoveryClientConfiguration.class })
	public static class TtlSchedulerTestConfig {

	}

}
