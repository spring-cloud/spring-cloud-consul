/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.consul.test.ConsulTestcontainers;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsulPropertyPrefixTests {

	private ConsulClient client;

	@Before
	public void setup() {
		ConsulTestcontainers.start();
		this.client = ConsulTestcontainers.client();
	}

	@After
	public void teardown() {
		this.client.deleteKVValues("");
	}

	@Test
	public void testEmptyPrefix() {
		// because prefix is empty, a leading forward slash is omitted
		String kvContext = "appname";
		this.client.setKVValue(kvContext + "/fooprop", "fookvval");
		this.client.setKVValue(kvContext + "/bar/prop", "8080");

		ConsulPropertySource source = getConsulPropertySource(new ConsulConfigProperties(), kvContext);
		assertProperties(source, "fookvval", "8080");
	}

	private void assertProperties(ConsulPropertySource source, Object fooval, Object barval) {
		assertThat(source.getProperty("fooprop")).as("fooprop was wrong").isEqualTo(fooval);
		assertThat(source.getProperty("bar.prop")).as("bar.prop was wrong").isEqualTo(barval);
	}

	@SuppressWarnings("Duplicates")
	private ConsulPropertySource getConsulPropertySource(ConsulConfigProperties configProperties, String context) {
		ConsulPropertySource source = new ConsulPropertySource(context, this.client, configProperties);
		source.init();
		String[] names = source.getPropertyNames();
		assertThat(names).as("names was null").isNotNull();
		assertThat(names.length).as("names was wrong size").isEqualTo(2);
		return source;
	}

}
