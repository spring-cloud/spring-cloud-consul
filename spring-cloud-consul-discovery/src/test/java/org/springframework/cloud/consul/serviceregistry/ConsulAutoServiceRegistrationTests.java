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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration.normalizeForDns;

/**
 * @author Spencer Gibb
 * @author Venil Noronha
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "spring.application.name=myTestService1-FF::something" },
		webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationTests {

	@Autowired
	private ConsulRegistration registration;

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get(this.registration.getInstanceId());
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort().intValue()).as("service port is 0").isNotEqualTo(0);
		assertThat(service.getId().contains(":"))
				.as("service id contained invalid character: " + service.getId())
				.isFalse();
		assertThat(service.getId()).as("service id was wrong")
				.isEqualTo(this.registration.getInstanceId());
		assertThat(service.getService()).as("service name was wrong")
				.isEqualTo("myTestService1-FF-something");
		assertThat(StringUtils.isEmpty(service.getAddress()))
				.as("service address must not be empty").isFalse();
		assertThat(service.getAddress())
				.as("service address must equals hostname from discovery properties")
				.isEqualTo(this.discoveryProperties.getHostname());
	}

	@Test
	public void normalizeForDnsWorks() {
		assertThat(normalizeForDns("abc1")).isEqualTo("abc1");
		assertThat(normalizeForDns("ab:c1")).isEqualTo("ab-c1");
		assertThat(normalizeForDns("ab::c1")).isEqualTo("ab-c1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void normalizedFailsIfFirstCharIsNumber() {
		normalizeForDns("9abc");
	}

	@Test(expected = IllegalArgumentException.class)
	public void normalizedFailsIfFirstCharIsNotAlpha() {
		normalizeForDns(":abc");
	}

	@Test(expected = IllegalArgumentException.class)
	public void normalizedFailsIfLastCharIsNotAlphaNumeric() {
		normalizeForDns("abc:");
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	protected static class TestConfig {

	}

}
