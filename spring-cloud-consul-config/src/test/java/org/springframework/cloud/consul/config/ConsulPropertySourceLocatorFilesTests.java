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

package org.springframework.cloud.consul.config;

import java.util.Collection;
import java.util.Iterator;
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
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.annotation.DirtiesContext;

import com.ecwid.consul.v1.ConsulClient;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
@DirtiesContext
public class ConsulPropertySourceLocatorFilesTests {
	public static final String PREFIX = "_propertySourceLocatorFilesTests_config__";
	public static final String ROOT = PREFIX + UUID.randomUUID();
	public static final String APP_NAME = "testFilesFormat";
	public static final String APPLICATION_YML = "/application.yml";
	public static final String APPLICATION_DEV_YML = "/application-dev.yaml";
	public static final String APP_NAME_PROPS = "/"+APP_NAME+".properties";
	public static final String APP_NAME_DEV_PROPS = "/"+APP_NAME+"-dev.properties";

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
		this.client.setKVValue(ROOT+ APPLICATION_YML, "foo: bar\nmy.baz: ${foo}");
		this.client.setKVValue(ROOT+ APPLICATION_DEV_YML, "foo: bar-dev\nmy.baz: ${foo}");
		this.client.setKVValue(ROOT+"/master.ref", UUID.randomUUID().toString());
		this.client.setKVValue(ROOT+APP_NAME_PROPS, "foo: bar-app\nmy.baz: ${foo}");
		this.client.setKVValue(ROOT+APP_NAME_DEV_PROPS, "foo: bar-app-dev\nmy.baz: ${foo}");

		this.context = new SpringApplicationBuilder(Config.class)
				.web(WebApplicationType.NONE)
				.run("--spring.application.name="+ APP_NAME,
						"--spring.cloud.consul.config.prefix="+ROOT,
						"--spring.cloud.consul.config.format=FILES",
						"--spring.profiles.active=dev",
						"spring.cloud.consul.config.watch.delay=1");

		this.client = context.getBean(ConsulClient.class);
		this.properties = context.getBean(ConsulProperties.class);
		this.environment = context.getEnvironment();
	}

	@After
	public void teardown() {
		this.client.deleteKVValues(PREFIX);
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void propertySourcesFound() throws Exception {
		String foo = this.environment.getProperty("foo");
		assertThat("foo was wrong", foo, is(equalTo("bar-app-dev")));

		String myBaz = this.environment.getProperty("my.baz");
		assertThat("my.baz was wrong", myBaz, is(equalTo("bar-app-dev")));

		MutablePropertySources propertySources = this.environment.getPropertySources();
		PropertySource<?> bootstrapProperties = propertySources.get("bootstrapProperties");
		assertThat("bootstrapProperties was null", bootstrapProperties, is(notNullValue()));
		assertThat("bootstrapProperties was wrong type", bootstrapProperties.getClass(), is(typeCompatibleWith(CompositePropertySource.class)));

		Collection<PropertySource<?>> consulSources = ((CompositePropertySource) bootstrapProperties).getPropertySources();
		assertThat("consulSources was wrong size", consulSources, hasSize(1));

		PropertySource<?> consulSource = consulSources.iterator().next();
		assertThat("consulSource was wrong type", consulSource.getClass(), is(typeCompatibleWith(CompositePropertySource.class)));
		Collection<PropertySource<?>> fileSources = ((CompositePropertySource) consulSource).getPropertySources();
		assertThat("fileSources was wrong size", fileSources, hasSize(4));

		assertFileSourceNames(fileSources, APP_NAME_DEV_PROPS, APP_NAME_PROPS, APPLICATION_DEV_YML, APPLICATION_YML);
	}

	private void assertFileSourceNames(Collection<PropertySource<?>> fileSources, String... names) {
		Iterator<PropertySource<?>> iterator = fileSources.iterator();
		for (String name : names) {
			PropertySource<?> fileSource = iterator.next();
			assertThat("fileSources was wrong name", fileSource.getName(), endsWith(name));
		}
	}
}
