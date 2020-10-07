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

package org.springframework.cloud.consul.discovery.configclient;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.cloud.test.ClassPathExclusions;
import org.springframework.cloud.test.ModifiedClassPathRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * @author Dave Syer
 */
@RunWith(ModifiedClassPathRunner.class)
@ClassPathExclusions({ "spring-retry-*.jar", "spring-boot-starter-aop-*.jar" })
public class DiscoveryClientConfigServiceAutoConfigurationTests {

	private ConfigurableApplicationContext context;

	@Before
	public void init() {
		// FIXME: why do I need to do this? (fails in maven build without it.
		TomcatURLStreamHandlerFactory.disable();
	}

	@After
	public void close() {
		if (this.context != null) {
			if (this.context.getParent() != null) {
				((AbstractApplicationContext) this.context.getParent()).close();
			}
			this.context.close();
		}
	}

	@Test
	public void onWhenRequested() throws Exception {
		ConsulTestcontainers.start();
		Integer port = ConsulTestcontainers.getPort();
		String host = ConsulTestcontainers.getHost();
		setup("server.port=0", "spring.cloud.config.discovery.enabled=true", "spring.cloud.consul.port=" + port,
				"spring.cloud.consul.host=" + host, "logging.level.org.springframework.cloud.config.client=DEBUG",
				"spring.cloud.consul.discovery.catalog-services-watch.enabled=false",
				"spring.cloud.consul.discovery.test.enabled:true",
				"spring.application.name=discoveryclientconfigservicetest", "spring.jmx.enabled=false",
				"spring.cloud.consul.discovery.port:7001", "spring.cloud.consul.discovery.hostname:foo",
				"spring.cloud.config.discovery.service-id:configserver");

		assertThat(this.context.getBeanNamesForType(ConsulConfigServerAutoConfiguration.class).length).isEqualTo(1);
		ConsulDiscoveryClient client = this.context.getParent().getBean(ConsulDiscoveryClient.class);
		verify(client, atLeast(2)).getInstances("configserver");
		ConfigClientProperties locator = this.context.getBean(ConfigClientProperties.class);
		assertThat(locator.getUri()[0]).isEqualTo("http://foo:7001/");
	}

	private void setup(String... env) {
		this.context = new SpringApplicationBuilder(TestConfig.class).properties(env).run();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	protected static class TestConfig {

	}

}
