/*
 * Copyright 2013-2024 the original author or authors.
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

package org.springframework.cloud.consul;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;
import org.mockserver.verify.VerificationTimes;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.cloud.consul.model.http.kv.GetValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

@Testcontainers
class IConsulClientIntegrationTests {

	@Container
	static MockServerContainer mockServerContainer = new MockServerContainer(
			DockerImageName.parse("mockserver/mockserver:5.15.0"));
	static MockServerClient mockServerClient;

	private IConsulClient client;

	@BeforeAll
	static void beforeAll() {
		mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort());
	}

	@BeforeEach
	void setUp() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		mockServerClient.reset();
		ConsulProperties consulProperties = new ConsulProperties();
		consulProperties.setHost(mockServerContainer.getHost());
		consulProperties.setPort(mockServerContainer.getServerPort());
		this.client = ConsulAutoConfiguration.createNewConsulClient(consulProperties);
	}

	@Test
	void getStatusLeader() {
		mockServerClient.when(request().withMethod("GET").withPath("/v1/status/leader"))
			.respond(response().withStatusCode(200)
				.withHeaders(new Header("Content-Type", "application/json"))
				.withBody("\"172.17.0.2:8300\""));

		String statusLeader = client.getStatusLeader();

		verifyRequestSentToConsul("GET", "/v1/status/leader", 1);

		// Verify response from consul
		assertThat(statusLeader).isEqualTo("\"172.17.0.2:8300\"");
	}

	@Test
	void getKVValues_WithWaitTimeAndIndexAndNoAclToken() {

		mockServerClient.when(request().withMethod("GET").withPath("/v1/kv/context"))
			.respond(response().withStatusCode(200)
				.withHeaders(new Header("Content-Type", "application/json"))
				.withBody(json("[\n" + "  {\n" + "    \"CreateIndex\": 100,\n" + "    \"ModifyIndex\": 200,\n"
						+ "    \"LockIndex\": 200,\n" + "    \"Key\": \"zip\",\n" + "    \"Flags\": 0,\n"
						+ "    \"Value\": \"dGVzdA==\",\n"
						+ "    \"Session\": \"adf4238a-882b-9ddc-4a9d-5b6758e4159e\"\n" + "  }\n" + "]\n")));

		ResponseEntity<List<GetValue>> response = client.getKVValues("context", null, 5L, 2);

		mockServerClient.verify(request().withMethod("GET")
			.withPath("/v1/kv/context")
			.withQueryStringParameters(Parameter.param("recurse"), Parameter.param("wait", "5s"),
					Parameter.param("index", "2")),
				VerificationTimes.exactly(1));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/json");
		assertThat(response.hasBody()).isTrue();

		List<GetValue> values = response.getBody();

		assertThat(values).hasSize(1);
		GetValue value = values.get(0);
		assertThat(value.getKey()).isEqualTo("zip");
		assertThat(value.getValue()).isEqualTo("dGVzdA==");
		assertThat(value.getDecodedValue()).isEqualTo("test");
	}

	@Test
	void getKVValues_WithWaitTimeAndIndexAndAclToken() {

		mockServerClient
			.when(request().withMethod("GET")
				.withHeader(Header.header("X-Consul-Token", "aclToken"))
				.withPath("/v1/kv/context"))
			.respond(response().withStatusCode(200)
				.withHeaders(new Header("Content-Type", "application/json"))
				.withBody(json("[\n" + "  {\n" + "    \"CreateIndex\": 100,\n" + "    \"ModifyIndex\": 200,\n"
						+ "    \"LockIndex\": 200,\n" + "    \"Key\": \"zip\",\n" + "    \"Flags\": 0,\n"
						+ "    \"Value\": \"dGVzdA==\",\n"
						+ "    \"Session\": \"adf4238a-882b-9ddc-4a9d-5b6758e4159e\"\n" + "  }\n" + "]\n")));

		ResponseEntity<List<GetValue>> response = client.getKVValues("context", "aclToken", 5L, 2);

		mockServerClient.verify(request().withMethod("GET")
			.withPath("/v1/kv/context")
			.withHeader(Header.header("X-Consul-Token", "aclToken"))
			.withQueryStringParameters(Parameter.param("recurse"), Parameter.param("wait", "5s"),
					Parameter.param("index", "2")),
				VerificationTimes.exactly(1));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/json");
		assertThat(response.hasBody()).isTrue();

		List<GetValue> values = response.getBody();

		assertThat(values).hasSize(1);
		GetValue value = values.get(0);
		assertThat(value.getKey()).isEqualTo("zip");
		assertThat(value.getValue()).isEqualTo("dGVzdA==");
		assertThat(value.getDecodedValue()).isEqualTo("test");
	}

	private void verifyRequestSentToConsul(String method, String path, int times) {
		mockServerClient.verify(request().withMethod(method).withPath(path), VerificationTimes.exactly(times));
	}

}
