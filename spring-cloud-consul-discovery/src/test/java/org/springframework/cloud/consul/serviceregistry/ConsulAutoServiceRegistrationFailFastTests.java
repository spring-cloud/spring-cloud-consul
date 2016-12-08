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

package org.springframework.cloud.consul.serviceregistry;

import org.junit.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 * @author Venil Noronha
 */
public class ConsulAutoServiceRegistrationFailFastTests {

	@Test(expected = ConsulException.class)
	public void testFailFastEnabled() {
		new SpringApplicationBuilder(TestConfig.class).properties("server.port=0", "spring.cloud.consul.discovery.failFast=true").run();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class, ConsulAutoServiceRegistrationAutoConfiguration.class })
	protected static class TestConfig {

		@Bean
		public ConsulClient consulClient() {
			return new ConsulClient("localhost", 4321);
		}
	}
}

