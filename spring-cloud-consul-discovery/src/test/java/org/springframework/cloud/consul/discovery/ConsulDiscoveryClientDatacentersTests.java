/*
 * Copyright 2013-present the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests that the discovery clients look up services in the datacenters configured with
 * {@link ConsulDiscoveryProperties#getDatacenters()}.
 *
 * @author Asish Kumar
 */
class ConsulDiscoveryClientDatacentersTests {

	private static final String HEALTH_SERVICE = """
			[{"Node": {"Node": "node1", "Address": "10.0.0.1", "Datacenter": "dc-west"},
			"Service": {"ID": "stores-1", "Service": "stores", "Address": "10.0.0.2", "Port": 8080}}]""";

	private MockRestServiceServer server;

	private ConsulClient client;

	private ConsulDiscoveryProperties properties;

	private ConsulDiscoveryClient discoveryClient;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8500");
		this.server = MockRestServiceServer.bindTo(builder).build();
		this.client = ConsulAutoConfiguration.createNewConsulClient(RestClientAdapter.create(builder.build()));
		this.properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		this.properties.getDatacenters().put("stores", "dc-west");
		this.discoveryClient = new ConsulDiscoveryClient(this.client, this.properties);
	}

	@Test
	void getInstancesQueriesConfiguredDatacenter() {
		expectStoresInDcWest();

		List<ServiceInstance> instances = this.discoveryClient.getInstances("stores");

		assertThat(instances).singleElement().satisfies(instance -> {
			assertThat(instance.getServiceId()).isEqualTo("stores");
			assertThat(instance.getHost()).isEqualTo("10.0.0.2");
			assertThat(instance.getPort()).isEqualTo(8080);
		});
		this.server.verify();
	}

	@Test
	void getInstancesQueriesLocalDatacenterForServiceWithoutConfiguredDatacenter() {
		this.server.expect(requestTo("http://localhost:8500/v1/health/service/customers?passing=false"))
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		assertThat(this.discoveryClient.getInstances("customers")).isEmpty();
		this.server.verify();
	}

	@Test
	void getInstancesQueriesConfiguredDatacenterWithConsistencyMode() {
		this.properties.setConsistencyMode(ConsulDiscoveryProperties.ConsistencyMode.STALE);
		this.server.expect(requestTo(startsWith("http://localhost:8500/v1/health/service/stores?")))
			.andExpect(queryParam("dc", "dc-west"))
			.andExpect(queryParam("stale", ""))
			.andRespond(withSuccess(HEALTH_SERVICE, MediaType.APPLICATION_JSON));

		assertThat(this.discoveryClient.getInstances("stores")).hasSize(1);
		this.server.verify();
	}

	@Test
	void getAllInstancesQueriesConfiguredDatacenterPerService() {
		this.server.expect(requestTo("http://localhost:8500/v1/catalog/services"))
			.andRespond(withSuccess("{\"customers\": [], \"stores\": []}", MediaType.APPLICATION_JSON));
		this.server.expect(requestTo("http://localhost:8500/v1/health/service/customers?passing=false"))
			.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		expectStoresInDcWest();

		assertThat(this.discoveryClient.getAllInstances()).singleElement()
			.extracting(ServiceInstance::getServiceId)
			.isEqualTo("stores");
		this.server.verify();
	}

	@Test
	void reactiveGetInstancesQueriesConfiguredDatacenter() {
		expectStoresInDcWest();
		ConsulReactiveDiscoveryClient reactiveDiscoveryClient = new ConsulReactiveDiscoveryClient(this.client,
				this.properties);

		StepVerifier.create(reactiveDiscoveryClient.getInstances("stores"))
			.assertNext(instance -> assertThat(instance.getHost()).isEqualTo("10.0.0.2"))
			.verifyComplete();
		this.server.verify();
	}

	private void expectStoresInDcWest() {
		this.server.expect(requestTo(startsWith("http://localhost:8500/v1/health/service/stores?")))
			.andExpect(queryParam("dc", "dc-west"))
			.andRespond(withSuccess(HEALTH_SERVICE, MediaType.APPLICATION_JSON));
	}

}
