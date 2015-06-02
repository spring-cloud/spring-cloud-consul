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

package org.springframework.cloud.ui;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestConfig.class)
@WebIntegrationTest(value = "spring.application.name=myTestService", randomPort = true)
public class EnableConsulUiTests {

	@Value("${local.server.port}")
	private int port;

	@Test
	public void consulWebUiWorks() {
		ResponseEntity<String> response = new TestRestTemplate().getForEntity("http://localhost:" + port + "/consul/ui/", String.class);
		assertEquals("Wrong response code", HttpStatus.OK, response.getStatusCode());
		String body = response.getBody();
		assertNotNull("Null body", body);
		assertTrue("Missing body text", body.toLowerCase().contains("consul") && body.toLowerCase().contains("services"));
	}

}

@Configuration
@EnableAutoConfiguration
@Import({ ConsulAutoConfiguration.class })
@EnableConsulUi
class TestConfig {

}