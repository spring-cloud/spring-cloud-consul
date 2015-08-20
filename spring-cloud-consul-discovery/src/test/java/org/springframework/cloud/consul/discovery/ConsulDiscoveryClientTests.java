/*
 * Copyright 2013-2015 the original author or authors.
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

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hamcrest.CustomMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.ServiceInstance.Capability;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ConsulDiscoveryClientTests.MyTestConfig.class)
@WebIntegrationTest(value = {"spring.application.name=testConsulDiscovery", "spring.cloud.consul.discovery.preferIpAddress=true"}, randomPort = true)
public class ConsulDiscoveryClientTests {

	@Autowired
	private ConsulDiscoveryClient discoveryClient;

	@Autowired
	private ConsulClient consul;

	@Test
	public void getInstancesForServiceWorks() {
		List<ServiceInstance> instances = discoveryClient.getInstances("consul");
		assertNotNull("instances was null", instances);
		assertFalse("instances was empty", instances.isEmpty());

		ServiceInstance instance = instances.get(0);
		assertIpAddress(instance);
	}

	private void assertIpAddress(ServiceInstance instance) {
		assertTrue("host isn't an ip address", Character.isDigit(instance.getHost().charAt(0)));
	}

	@Test
	public void getLocalInstance() {
		ServiceInstance instance = discoveryClient.getLocalServiceInstance();
		assertNotNull("instance was null", instance);
		assertIpAddress(instance);
	}

	@Before
	public void registerFoo() {
		final NewService foo1 = new NewService();
		foo1.setId("foo1");
		foo1.setName("foo");
		foo1.setAddress("127.0.0.1");
		foo1.setPort(Integer.valueOf(9000));
		foo1.setTags(Arrays.asList("1"));

		consul.agentServiceRegister(foo1);

		final NewService foo2 = new NewService();
		foo2.setId("foo2");
		foo2.setName("foo");
		foo2.setAddress("127.0.0.1");
		foo2.setPort(Integer.valueOf(9001));
		foo2.setTags(Arrays.asList("2"));

		consul.agentServiceRegister(foo2);
	}

	@After
	public void deregisterFoo() {
		consul.agentServiceDeregister("foo1");
		consul.agentServiceDeregister("foo2");
	}

	@Test
	public void getServiceTags() {
		final List<ServiceInstance> fooInstances = discoveryClient.getInstances("foo");
		assertEquals(2, fooInstances.size());

		class TagMatcher extends CustomMatcher<ServiceInstance> {

			final Set<String> expected;

			TagMatcher(String... expectedTags) {
				super(String.valueOf(expectedTags));
				this.expected = new HashSet<>(Arrays.asList(expectedTags));
			}

			@Override
			public boolean matches(Object item) {
				if (item instanceof ServiceInstance) {
					final ServiceInstance serviceInstance = (ServiceInstance) item;
					if (serviceInstance.supports(Capability.TAGS)) {
						return Objects.equals(expected, serviceInstance.getValue(Capability.TAGS));
					}
				}
				return false;
			}

		}
		assertThat(fooInstances, hasItems(new TagMatcher("1"), new TagMatcher("2")));
	}

	@Configuration
	@EnableDiscoveryClient
	@EnableAutoConfiguration
	@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
	public static class MyTestConfig {

	}
}
