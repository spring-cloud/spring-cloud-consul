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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.agent.model.NewService;

/**
 * @author Lomesh Patel
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoRegistrationHealthCheckHeadersTests.TestConfig.class, properties = {
		"spring.application.name=myTestService-DiscoveryHealthCheckTlsSkipVerify",
		"spring.cloud.consul.discovery.health-check-headers.X-Config-Token=ACCESSTOKEN" }, webEnvironment = RANDOM_PORT)
public class ConsulAutoRegistrationHealthCheckHeadersTests {

	@Autowired
	private ConsulAutoRegistration registration;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Test
	public void contextLoads() {
		NewService service = this.registration.getService();
		assertThat(service).as("service was null").isNotNull();

		NewService.Check check = service.getCheck();
		assertThat(check).as("check was null").isNotNull();
		assertThat(check.getHeader()).as("header is null").isNotNull();
		assertThat(check.getHeader()).as("header is empty").isNotEmpty();
		assertThat(check.getHeader().get("X-Config-Token").get(0))
				.as("expected header value not found").isEqualTo("ACCESSTOKEN");

		// unable to call consul api to get health check details
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestConfig {
	}
}
