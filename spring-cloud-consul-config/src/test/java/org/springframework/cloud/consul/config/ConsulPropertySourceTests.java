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

import java.util.Random;

import com.ecwid.consul.v1.ConsulClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.consul.test.ConsulTestcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceTests {

	private ConsulClient client;

	private String prefix;

	private String kvContext;

	private String propertiesContext;

	@Before
	public void setup() {
		ConsulTestcontainers.start();
		this.prefix = "consulPropertySourceTests" + new Random().nextInt(Integer.MAX_VALUE);
		this.client = ConsulTestcontainers.client();
	}

	@After
	public void teardown() {
		this.client.deleteKVValues(this.prefix);
	}

	@Test
	public void testKv() {
		// key value properties
		this.kvContext = this.prefix + "/kv";
		this.client.setKVValue(this.kvContext + "/fooprop", "fookvval");
		this.client.setKVValue(this.prefix + "/kv" + "/bar/prop", "8080");

		ConsulPropertySource source = getConsulPropertySource(new ConsulConfigProperties(), this.kvContext);

		assertProperties(source, "fookvval", "8080");
	}

	private void assertProperties(ConsulPropertySource source, Object fooval, Object barval) {
		assertThat(source.getProperty("fooprop")).as("fooprop was wrong").isEqualTo(fooval);
		assertThat(source.getProperty("bar.prop")).as("bar.prop was wrong").isEqualTo(barval);
	}

	@Test
	public void testProperties() {
		// properties file property
		this.propertiesContext = this.prefix + "/properties";
		this.client.setKVValue(this.propertiesContext + "/data", "fooprop=foopropval\nbar.prop=8080");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.PROPERTIES);
		ConsulPropertySource source = getConsulPropertySource(configProperties, this.propertiesContext);

		assertProperties(source, "foopropval", "8080");
	}

	@Test
	public void testYaml() {
		// yaml file property
		String yamlContext = this.prefix + "/yaml";
		this.client.setKVValue(yamlContext + "/data", "fooprop: fooymlval\nbar:\n  prop: 8080");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.YAML);
		ConsulPropertySource source = getConsulPropertySource(configProperties, yamlContext);

		assertProperties(source, "fooymlval", 8080);
	}

	@Test
	public void testEmptyYaml() {
		// yaml file property
		String yamlContext = this.prefix + "/yaml";
		this.client.setKVValue(yamlContext + "/data", "");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.YAML);
		ConsulPropertySource source = new ConsulPropertySource(yamlContext, this.client, configProperties);
		// Should NOT throw a NPE
		source.init();
	}

	private ConsulPropertySource getConsulPropertySource(ConsulConfigProperties configProperties, String context) {
		ConsulPropertySource source = new ConsulPropertySource(context, this.client, configProperties);
		source.init();
		String[] names = source.getPropertyNames();
		assertThat(names).as("names was null").isNotNull();
		assertThat(names.length).as("names was wrong size").isEqualTo(2);
		return source;
	}

}
