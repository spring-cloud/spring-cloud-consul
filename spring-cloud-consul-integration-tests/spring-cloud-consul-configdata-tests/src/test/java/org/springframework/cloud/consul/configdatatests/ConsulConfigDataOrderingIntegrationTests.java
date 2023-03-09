/*
 * Copyright 2018-2019 the original author or authors.
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

package org.springframework.cloud.consul.configdatatests;

import java.util.Map;
import java.util.UUID;

import com.ecwid.consul.v1.ConsulClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = ConsulConfigDataApplication.class,
		properties = { "spring.application.name=" + ConsulConfigDataOrderingIntegrationTests.APP_NAME,
				"spring.config.name=orderingtest", "spring.profiles.active=dev",
				"management.endpoints.web.exposure.include=*", "management.endpoint.env.show-values=ALWAYS" },
		webEnvironment = RANDOM_PORT)
public class ConsulConfigDataOrderingIntegrationTests {

	private static final String BASE_PATH = new WebEndpointProperties().getBasePath();

	static final String APP_NAME = "testConsulConfigDataOrderingIntegration";

	private static final String PREFIX = "_configDataOrderingIntegrationTests_config__";

	private static final String ROOT = PREFIX + UUID.randomUUID();

	private static final String VALUE = "my value from consul default profile";

	private static final String TEST_PROP = "my.prop";

	private static final String KEY = ROOT + "/" + APP_NAME + "/" + TEST_PROP;

	private static final String VALUE_PROFILE = "my value from consul dev profile";

	private static final String KEY_PROFILE = ROOT + "/" + APP_NAME + ",dev/" + TEST_PROP;

	@Autowired
	private Environment env;

	@BeforeAll
	public static void initialize() {
		ConsulTestcontainers.initializeSystemProperties();
		System.setProperty(ConsulConfigProperties.PREFIX + ".prefix", ROOT);
		ConsulClient client = ConsulTestcontainers.client();
		client.deleteKVValues(PREFIX);
		client.setKVValue(KEY, VALUE);
		client.setKVValue(KEY_PROFILE, VALUE_PROFILE);
	}

	@AfterAll
	public static void close() {
		System.clearProperty(ConsulProperties.PREFIX + ".port");
		System.clearProperty(ConsulProperties.PREFIX + ".host");
		System.clearProperty(ConsulProperties.PREFIX + ".prefix");
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void contextLoads() {
		Integer port = env.getProperty("local.server.port", Integer.class);
		ResponseEntity<Map> response = new TestRestTemplate()
				.getForEntity("http://localhost:" + port + BASE_PATH + "/env/my.prop", Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Map res = response.getBody();
		assertThat(res).containsKey("propertySources");
		Map<String, Object> property = (Map<String, Object>) res.get("property");
		assertThat(property).containsEntry("value", VALUE_PROFILE);
	}

}
