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

package org.springframework.cloud.consul.discovery.configclient;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Dave Syer
 */
public class ConsulConfigServerAutoConfigurationTests {

	private ConfigurableApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void offByDefault() throws Exception {
		this.context = new AnnotationConfigApplicationContext(
				ConsulConfigServerAutoConfiguration.class);
		assertEquals(0,
				this.context.getBeanNamesForType(ConsulDiscoveryProperties.class).length);
	}

	@Test
	public void onWhenRequested() throws Exception {
		setup("spring.cloud.config.server.prefix=/config");
		assertEquals(1,
				this.context.getBeanNamesForType(ConsulDiscoveryProperties.class).length);
		ConsulDiscoveryProperties properties = this.context.getBean(ConsulDiscoveryProperties.class);
		assertThat(properties.getTags(), contains("configPath=/config"));
	}

	private void setup(String... env) {
		this.context = new SpringApplicationBuilder(
				PropertyPlaceholderAutoConfiguration.class,
				ConsulConfigServerAutoConfiguration.class,
				ConfigServerProperties.class, ConsulDiscoveryProperties.class).web(WebApplicationType.NONE)
				.properties(env).run();
	}

}
