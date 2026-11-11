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

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests {@link ConsulDiscoveryClient#getAllInstances()} through the HTTP service proxy,
 * which rejects arguments that a mocked {@link ConsulClient} would accept.
 *
 * @author Asish Kumar
 */
class ConsulDiscoveryClientAllInstancesTests {

	private static final String ACL_TOKEN = "2d2e6b3b-1c82-40ab-8171-54609d8ad304";

	private static final String HEALTH_SERVICE = """
			[{"Node": {"Node": "node1", "Address": "10.0.0.1"},
			"Service": {"ID": "my-service-1", "Service": "my-service", "Address": "10.0.0.2", "Port": 8080}}]""";

	private MockRestServiceServer server;

	private ConsulDiscoveryProperties properties;

	private ConsulDiscoveryClient discoveryClient;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8500");
		this.server = MockRestServiceServer.bindTo(builder).build();
		ConsulClient client = ConsulAutoConfiguration
			.createNewConsulClient(RestClientAdapter.create(builder.build()));
		this.properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		this.properties.setAclToken(ACL_TOKEN);
		this.discoveryClient = new ConsulDiscoveryClient(client, this.properties);
	}

	@Test
	void getAllInstancesReturnsInstancesOfEveryService() {
		expectCatalogServices();
		this.server.expect(requestTo("http://localhost:8500/v1/health/service/my-service?passing=false"))
			.andExpect(header(ConsulClient.ACL_TOKEN_HEADER, ACL_TOKEN))
			.andRespond(withSuccess(HEALTH_SERVICE, MediaType.APPLICATION_JSON));

		List<ServiceInstance> instances = this.discoveryClient.getAllInstances();

		assertThat(instances).singleElement().satisfies(instance -> {
			assertThat(instance.getServiceId()).isEqualTo("my-service");
			assertThat(instance.getHost()).isEqualTo("10.0.0.2");
			assertThat(instance.getPort()).isEqualTo(8080);
		});
		this.server.verify();
	}

	@Test
	void getAllInstancesUsesConfiguredConsistencyMode() {
		this.properties.setConsistencyMode(ConsulDiscoveryProperties.ConsistencyMode.STALE);
		expectCatalogServices();
		this.server.expect(requestTo(startsWith("http://localhost:8500/v1/health/service/my-service")))
			.andExpect(queryParam("stale", ""))
			.andRespond(withSuccess(HEALTH_SERVICE, MediaType.APPLICATION_JSON));

		assertThat(this.discoveryClient.getAllInstances()).hasSize(1);
		this.server.verify();
	}

	private void expectCatalogServices() {
		this.server.expect(requestTo("http://localhost:8500/v1/catalog/services"))
			.andExpect(header(ConsulClient.ACL_TOKEN_HEADER, ACL_TOKEN))
			.andRespond(withSuccess("{\"my-service\": []}", MediaType.APPLICATION_JSON));
	}

}
