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

package org.springframework.cloud.consul.config;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.test.ConsulTestcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceTests {

	private ConsulClient consulClient;

	private String prefix;

	private String kvContext;

	private String propertiesContext;

	@Before
	public void setup() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		ConsulTestcontainers.start();
		this.prefix = "/consulPropertySourceTests" + new Random().nextInt(Integer.MAX_VALUE);
		this.consulClient = ConsulTestcontainers.client();
	}

	@After
	public void teardown() {
		this.consulClient.deleteKVValues(this.prefix);
	}

	@Test
	public void testKv() {
		// key value properties
		this.kvContext = this.prefix + "/kv";
		this.consulClient.setKVValue(this.kvContext + "/fooprop", "fookvval");
		this.consulClient.setKVValue(this.prefix + "/kv" + "/bar/prop", "8080");

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
		this.consulClient.setKVValue(this.propertiesContext + "/data", "fooprop=foopropval\nbar.prop=8080");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.PROPERTIES);
		ConsulPropertySource source = getConsulPropertySource(configProperties, this.propertiesContext);

		assertProperties(source, "foopropval", "8080");
	}

	@Test
	public void testYaml() {
		// yaml file property
		String yamlContext = this.prefix + "/yaml";
		this.consulClient.setKVValue(yamlContext + "/data", "fooprop: fooymlval\nbar:\n  prop: 8080");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.YAML);
		ConsulPropertySource source = getConsulPropertySource(configProperties, yamlContext);

		assertProperties(source, "fooymlval", 8080);
	}

	@Test
	public void testEmptyYaml() {
		// yaml file property
		String yamlContext = this.prefix + "/yaml";
		this.consulClient.setKVValue(yamlContext + "/data", "");

		ConsulConfigProperties configProperties = new ConsulConfigProperties();
		configProperties.setFormat(ConsulConfigProperties.Format.YAML);
		ConsulPropertySource source = new ConsulPropertySource(yamlContext, this.consulClient, configProperties);
		// Should NOT throw a NPE
		source.init();
	}

	private ConsulPropertySource getConsulPropertySource(ConsulConfigProperties configProperties, String context) {
		ConsulPropertySource source = new ConsulPropertySource(context, this.consulClient, configProperties);
		source.init();
		String[] names = source.getPropertyNames();
		assertThat(names).as("names was null").isNotNull();
		assertThat(names.length).as("names was wrong size").isEqualTo(2);
		return source;
	}

	@Test
	public void testExtraSlashInContext() {
		ConsulPropertySource source = new ConsulPropertySource("/my/very/custom/context", this.consulClient,
				new ConsulConfigProperties());
		source.init();
		assertThat(source.getContext()).as("context was wrong").isEqualTo("my/very/custom/context/");
	}

}
