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

package org.springframework.cloud.consul.serviceregistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulLifecycle;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=myTestService2-UU" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationWithLifecycleTests {

	@Autowired(required = false)
	private ConsulRegistration consulRegistration;

	@Autowired(required = false)
	private ConsulAutoServiceRegistration autoServiceRegistration;

	@Autowired(required = false)
	private ConsulServiceRegistry consulServiceRegistry;

	@Test
	public void contextLoads() {
		assertNull("consulRegistration was created by mistake", consulRegistration);
		assertNull("autoServiceRegistration was created by mistake", autoServiceRegistration);
		assertNotNull("consulServiceRegistry was not created", consulServiceRegistry);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class, ConsulAutoServiceRegistrationAutoConfiguration.class })
	protected static class TestConfig {
		@Bean
		public ConsulLifecycle consulLifecycle(ConsulClient client, ConsulDiscoveryProperties properties, HeartbeatProperties heartbeatProperties) {
			return new ConsulLifecycle(client, properties, heartbeatProperties);
		}
	}
}

