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

import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 * @author Venil Noronha
 * @deprecated remove in Edgware
 */
@Deprecated
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = TestConfig.class,
	properties = { "spring.application.name=myTestService1-F::something",
		"spring.cloud.consul.discovery.failFast=true" },
		webEnvironment = RANDOM_PORT)
public class ConsulLifecycleTests {

	@Autowired
	private ConsulLifecycle lifecycle;

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get(lifecycle.getServiceId());
		assertNotNull("service was null", service);
		assertNotEquals("service port is 0", 0, service.getPort().intValue());
		assertFalse("service id contained invalid character: " + service.getId(), service.getId().contains(":"));
		assertEquals("service id was wrong", lifecycle.getServiceId(), service.getId());
		assertEquals("service name was wrong", "myTestService1-F-something", service.getService());
		assertFalse("service address must not be empty", StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties", discoveryProperties.getHostname(), service.getAddress());
	}

	@Test
	public void normalizeForDnsWorks() {
		assertEquals("abc1", ConsulLifecycle.normalizeForDns("abc1"));
		assertEquals("ab-c1", ConsulLifecycle.normalizeForDns("ab:c1"));
		assertEquals("ab-c1", ConsulLifecycle.normalizeForDns("ab::c1"));
	}

	@Test(expected = ConsulException.class)
	public void testFailFastEnabled() {
		lifecycle.register(new NewService());
	}

	@Test(expected = IllegalArgumentException.class)
	public void normalizedFailsIfFirstCharIsNumber() {
		ConsulLifecycle.normalizeForDns("9abc");
	}

	@Test(expected = IllegalArgumentException.class)
	public void normalizedFailsIfFirstCharIsNotAlpha() {
		ConsulLifecycle.normalizeForDns(":abc");
	}

	@Test(expected = IllegalArgumentException.class)
	public void normalizedFailsIfLastCharIsNotAlphaNumeric() {
		ConsulLifecycle.normalizeForDns("abc:");
	}
}

@Configuration
@EnableAutoConfiguration
@Import({ TestConsulLifecycleConfiguration.class, ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
class TestConfig {

}
