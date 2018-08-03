/*
 * Copyright 2013-2017 the original author or authors.
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

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.annotation.DirtiesContext;

import com.ecwid.consul.v1.ConsulClient;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
@DirtiesContext
public class ConsulPropertySourceLocatorAppNameCustomizedTests {
	private static final String PREFIX = "_propertySourceLocatorAppNameCustomizedTests_config__";
	private static final String ROOT = PREFIX + UUID.randomUUID();
	private static final String VALUE1 = "testPropVal";
	private static final String TEST_PROP = "testProp";
	private static final String CONFIG_NAME = "myconfigfolder";
	private static final String KEY1 = ROOT + "/"+ CONFIG_NAME +"/"+ TEST_PROP;
	private static final String VALUE2 = "testPropVal2";
	private static final String TEST_PROP2 = "testProp2";
	private static final String KEY2 = ROOT + "/"+ CONFIG_NAME +"/"+ TEST_PROP2;

	private ConfigurableApplicationContext context;

	@Configuration
	@EnableAutoConfiguration
	static class Config {

	}

	private ConfigurableEnvironment environment;
	private ConsulClient client;
	private ConsulProperties properties;

	@Before
	public void setup() {
		this.properties = new ConsulProperties();
		this.client = new ConsulClient(properties.getHost(), properties.getPort());
		this.client.deleteKVValues(PREFIX);
		this.client.setKVValue(KEY1, VALUE1);
		this.client.setKVValue(KEY2, VALUE2);


		this.context = new SpringApplicationBuilder(Config.class)
				.web(WebApplicationType.NONE)
				.run("--spring.application.name=testConsulPropertySourceLocatorAppNameCustomized",
						"--spring.cloud.consul.config.name="+CONFIG_NAME,
						"--spring.cloud.consul.config.prefix="+ROOT);

		this.client = context.getBean(ConsulClient.class);
		this.properties = context.getBean(ConsulProperties.class);
		this.environment = context.getEnvironment();
	}

	@After
	public void teardown() {
		this.client.deleteKVValues(PREFIX);
		this.context.close();
	}

	@Test
	public void propertyLoaded() throws Exception {
		String testProp = this.environment.getProperty(TEST_PROP);
		assertThat("testProp was wrong", testProp, is(equalTo(VALUE1)));

		String testProp2 = this.environment.getProperty(TEST_PROP2);
		assertThat("testProp2 was wrong", testProp2, is(equalTo(VALUE2)));
	}
}
