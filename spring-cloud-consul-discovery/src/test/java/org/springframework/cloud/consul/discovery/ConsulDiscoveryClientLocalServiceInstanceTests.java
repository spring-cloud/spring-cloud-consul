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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.transport.RawResponse;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK,
		classes = ConsulDiscoveryClientLocalServiceInstanceTests.LocalServiceTestConfig.class,
		properties = {"spring.cloud.consul.discovery.catalogServicesWatch.enabled=false"})
public class ConsulDiscoveryClientLocalServiceInstanceTests {

	private static final String SERVICE_ID = "service1:8080";
	private static final String ADDRESS = "service1addr";
	private static final String SERVICE = "service1";
	private static final int PORT = 8080;
	private static final String KEY = "foo";
	private static final String VALUE = "bar";
	private static final String TAG = KEY+"="+VALUE;

	@MockBean
	private ConsulClient consul;

	@MockBean
	private ConsulLifecycle lifecycle;

	@MockBean
	private ConsulDiscoveryProperties properties;

	@Autowired
	private ConsulDiscoveryClient discoveryClient;
	public static final RawResponse RAW_RESPONSE = new RawResponse(200, null, null, 1L, false, null);

	@Test
	public void localServiceInstanceFromConsul() {
		Service service = new Service();
		service.setAddress(ADDRESS);
		service.setService(SERVICE);
		service.setPort(PORT);
		service.setId(SERVICE_ID);
		service.setTags(Arrays.asList(TAG));

		given(this.lifecycle.getServiceId()).willReturn(SERVICE_ID);

		given(this.consul.getAgentServices()).willReturn(new Response<>(Collections.singletonMap(SERVICE_ID, service), RAW_RESPONSE));

		ServiceInstance serviceInstance = this.discoveryClient.getLocalServiceInstance();

		assertServiceInstance(serviceInstance);
	}

	@Test
	public void localServiceInstanceFromConfig() {
		mockFromConfig(PORT, ADDRESS);

		ServiceInstance serviceInstance = this.discoveryClient.getLocalServiceInstance();

		assertServiceInstance(serviceInstance);
	}

	@Test
	public void localServiceInstanceFromConfigPortFromServerProperties() {
		mockFromConfig(0, ADDRESS);

		ServerProperties serverProperties = new ServerProperties();
		serverProperties.setPort(PORT);
		this.discoveryClient.setServerProperties(serverProperties);

		ServiceInstance serviceInstance = this.discoveryClient.getLocalServiceInstance();

		this.discoveryClient.setServerProperties(null);

		assertServiceInstance(serviceInstance);
	}

	@Test
	public void localServiceInstanceFromConfigHostFromAgent() {
		mockFromConfig(PORT, null);
		given(this.properties.isPreferAgentAddress()).willReturn(true);
		Self self = new Self();
		Member member = new Member();
		member.setName(ADDRESS);
		self.setMember(member);
		given(this.consul.getAgentSelf()).willReturn(new Response<>(self, RAW_RESPONSE));

		ServiceInstance serviceInstance = this.discoveryClient.getLocalServiceInstance();

		assertServiceInstance(serviceInstance);
	}

	private void assertServiceInstance(ServiceInstance serviceInstance) {
		assertThat(serviceInstance.getHost()).isEqualTo(ADDRESS);
		assertThat(serviceInstance.getPort()).isEqualTo(PORT);
		assertThat(serviceInstance.getServiceId()).isEqualTo(SERVICE_ID);
		assertThat(serviceInstance.getMetadata()).isNotEmpty().hasSize(1).contains(entry(KEY, VALUE));
	}

	private void mockFromConfig(int port, String address) {
		given(this.lifecycle.getServiceId()).willReturn(SERVICE_ID);
		given(this.lifecycle.getConfiguredPort()).willReturn(port);
		given(this.properties.getTags()).willReturn(Arrays.asList(TAG));
		given(this.properties.getHostname()).willReturn(address);
		given(this.properties.getLifecycle()).willReturn(new ConsulDiscoveryProperties.Lifecycle());

		given(this.consul.getAgentServices()).willReturn(new Response<>(Collections.<String, Service>emptyMap(), RAW_RESPONSE));
	}

	@Configuration
	@EnableDiscoveryClient
	@ImportAutoConfiguration({ ConsulDiscoveryClientConfiguration.class })
	protected static class LocalServiceTestConfig {

	}
}
