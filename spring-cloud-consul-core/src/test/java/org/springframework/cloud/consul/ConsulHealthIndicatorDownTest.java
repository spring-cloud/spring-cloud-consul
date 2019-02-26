/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.consul;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Lomesh Patel (lomeshpatel)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.consul.host=invalidhost")
public class ConsulHealthIndicatorDownTest {

	@Autowired
	private HealthEndpoint healthEndpoint;

	@Test
	public void doHealthCheck() {
		assertThat(this.healthEndpoint.health().getStatus())
				.as("health status was not DOWN").isEqualTo(Status.DOWN);
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	protected static class TestConfig {

	}

}
