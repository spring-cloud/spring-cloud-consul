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

package org.springframework.cloud.consul.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Random;

import com.ecwid.consul.v1.ConsulClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.consul.ConsulProperties;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceTests {

	private ConsulClient client;
	private ConsulProperties properties;
	private String prefix;
	private String kvContext;
	private String propertiesContext;

	@Before
	public void setup() {
		properties = new ConsulProperties();
		prefix = "consulPropertySourceTests" + new Random().nextInt(Integer.MAX_VALUE);
		client = new ConsulClient(properties.getHost(), properties.getPort());
	}

	@After
	public void teardown() {
		client.deleteKVValues(prefix);
	}

	@Test
	public void testKv() {
		// key value properties
		kvContext = prefix + "/kv";
		client.setKVValue(kvContext + "/fooprop", "fookvval");
		client.setKVValue(prefix+"/kv"+"/bar/prop", "8080");

		ConsulPropertySource source = getConsulPropertySource(new ConsulConfigProperties(), kvContext);

		assertProperties(source, "fookvval", "8080");
	}

	private void assertProperties(ConsulPropertySource source, Object fooval, Object barval) {
		assertThat("fooprop was wrong", source.getProperty("fooprop"), is(equalTo(fooval)));
		assertThat("bar.prop was wrong", source.getProperty("bar.prop"), is(equalTo(barval)));
	}

	@Test
	public void testProperties() {
		// properties file property
		propertiesContext = prefix + "/properties";
		client.setKVValue(propertiesContext+"/data", "fooprop=foopropval\nbar.prop=8080");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.PROPERTIES);
		ConsulPropertySource source = getConsulPropertySource(configProperties, propertiesContext);

		assertProperties(source, "foopropval", "8080");
	}

	@Test
	public void testYaml() {
		// yaml file property
		String yamlContext = prefix + "/yaml";
		client.setKVValue(yamlContext+"/data", "fooprop: fooymlval\nbar:\n  prop: 8080");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.YAML);
		ConsulPropertySource source = getConsulPropertySource(configProperties, yamlContext);

		assertProperties(source, "fooymlval", 8080);
	}

	@Test
	public void testEmptyYaml() {
		// yaml file property
		String yamlContext = prefix + "/yaml";
		client.setKVValue(yamlContext+"/data", "");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.YAML);
		ConsulPropertySource source = new ConsulPropertySource(yamlContext, client, configProperties);
		// Should NOT throw a NPE
		source.init();
	}

	private ConsulPropertySource getConsulPropertySource(ConsulConfigProperties configProperties, String context) {
		ConsulPropertySource source = new ConsulPropertySource(context, client, configProperties);
		source.init();
		String[] names = source.getPropertyNames();
		assertThat("names was null", names, is(notNullValue()));
		assertThat("names was wrong size", names.length, is(equalTo(2)));
		return source;
	}
}
