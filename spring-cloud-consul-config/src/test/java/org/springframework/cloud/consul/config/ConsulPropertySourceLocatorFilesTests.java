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

package org.springframework.cloud.consul.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import com.ecwid.consul.v1.ConsulClient;
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

import static org.assertj.core.api.Assertions.assertThat;

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

	public static final String APP_NAME_PROPS = "/" + APP_NAME + ".properties";

	public static final String APP_NAME_DEV_PROPS = "/" + APP_NAME + "-dev.properties";

	private ConfigurableApplicationContext context;

	private ConfigurableEnvironment environment;

	private ConsulClient client;

	private ConsulProperties properties;

	@Before
	public void setup() {
		this.properties = new ConsulProperties();
		this.client = new ConsulClient(this.properties.getHost(),
				this.properties.getPort());
		this.client.setKVValue(ROOT + APPLICATION_YML, "foo: bar\nmy.baz: ${foo}");
		this.client.setKVValue(ROOT + APPLICATION_DEV_YML,
				"foo: bar-dev\nmy.baz: ${foo}");
		this.client.setKVValue(ROOT + "/master.ref", UUID.randomUUID().toString());
		this.client.setKVValue(ROOT + APP_NAME_PROPS, "foo: bar-app\nmy.baz: ${foo}");
		this.client.setKVValue(ROOT + APP_NAME_DEV_PROPS,
				"foo: bar-app-dev\nmy.baz: ${foo}");

		this.context = new SpringApplicationBuilder(Config.class)
				.web(WebApplicationType.NONE).run("--spring.application.name=" + APP_NAME,
						"--spring.cloud.consul.config.prefix=" + ROOT,
						"--spring.cloud.consul.config.format=FILES",
						"--spring.profiles.active=dev",
						"spring.cloud.consul.config.watch.delay=1");

		this.client = this.context.getBean(ConsulClient.class);
		this.properties = this.context.getBean(ConsulProperties.class);
		this.environment = this.context.getEnvironment();
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
		assertThat(foo).as("foo was wrong").isEqualTo("bar-app-dev");

		String myBaz = this.environment.getProperty("my.baz");
		assertThat(myBaz).as("my.baz was wrong").isEqualTo("bar-app-dev");

		MutablePropertySources propertySources = this.environment.getPropertySources();
		PropertySource<?> bootstrapProperties = propertySources
				.get("bootstrapProperties");
		assertThat(bootstrapProperties).as("bootstrapProperties was null").isNotNull();
		assertThat(bootstrapProperties).as("bootstrapProperties was wrong type")
				.isInstanceOf(CompositePropertySource.class);

		Collection<PropertySource<?>> consulSources = ((CompositePropertySource) bootstrapProperties)
				.getPropertySources();
		assertThat(consulSources).as("consulSources was wrong size").hasSize(1);

		PropertySource<?> consulSource = consulSources.iterator().next();
		assertThat(consulSource).as("consulSource was wrong type")
				.isInstanceOf(CompositePropertySource.class);
		Collection<PropertySource<?>> fileSources = ((CompositePropertySource) consulSource)
				.getPropertySources();
		assertThat(fileSources).as("fileSources was wrong size").hasSize(4);

		assertFileSourceNames(fileSources, APP_NAME_DEV_PROPS, APP_NAME_PROPS,
				APPLICATION_DEV_YML, APPLICATION_YML);
	}

	private void assertFileSourceNames(Collection<PropertySource<?>> fileSources,
			String... names) {
		Iterator<PropertySource<?>> iterator = fileSources.iterator();
		for (String name : names) {
			PropertySource<?> fileSource = iterator.next();
			assertThat(fileSource.getName()).as("fileSources was wrong name")
					.endsWith(name);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {

	}

}
