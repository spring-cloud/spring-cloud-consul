/*
 * Copyright 2019-2019 the original author or authors.
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

package org.springframework.cloud.consul.discovery;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Fail.fail;

/**
 * @author Olga Maciaszek-Sharma
 */
public class ConsulDiscoveryClientConfigurationTests {

	private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

	@AfterEach
	public void after() {
		if (this.context != null && this.context.isActive()) {
			this.context.close();
		}
	}

	@Test
	public void consulConfigNotLoadedWhenCloudDiscoveryClientDisabled() {
		TestPropertyValues.of("spring.cloud.discovery.enabled=false").applyTo(this.context);
		setupContext();
		assertBeanNotPresent(ConsulDiscoveryProperties.class);
		assertBeanNotPresent(ConsulDiscoveryClient.class);
		assertBeanNotPresent(HeartbeatProperties.class);
	}

	@Test
	public void consulConfigIsLoadedWhenCloudDiscoveryClientEnabled() {
		TestPropertyValues.of("spring.cloud.discovery.enabled=true").applyTo(this.context);
		setupContext();
		assertBeanIsPresent(ConsulDiscoveryProperties.class);
		assertBeanIsPresent(ConsulDiscoveryClient.class);
	}

	@Test
	public void consulConfigNotLoadedWhenConsulDiscoveryClientDisabled() {
		TestPropertyValues.of("spring.cloud.consul.discovery.enabled=false").applyTo(this.context);
		setupContext();
		assertBeanNotPresent(ConsulDiscoveryProperties.class);
		assertBeanNotPresent(ConsulDiscoveryClient.class);
		assertBeanNotPresent(HeartbeatProperties.class);
	}

	@Test
	public void consulConfigIsLoadedWhenConsulDiscoveryClientEnabled() {
		TestPropertyValues.of("spring.cloud.consul.discovery.enabled=true").applyTo(this.context);
		setupContext();
		assertBeanIsPresent(ConsulDiscoveryProperties.class);
		assertBeanIsPresent(ConsulDiscoveryClient.class);
	}

	@Test
	public void consulConfigNotLoadedWhenCloudDiscoveryDisabled_ConsulDiscoveryClientDisabled() {
		TestPropertyValues.of("spring.cloud.discovery.enabled=false", "spring.cloud.consul.discovery.enabled=false")
			.applyTo(this.context);
		setupContext();
		assertBeanNotPresent(ConsulDiscoveryProperties.class);
		assertBeanNotPresent(ConsulDiscoveryClient.class);
		assertBeanNotPresent(HeartbeatProperties.class);
	}

	@Test
	public void consulConfigIsLoadedWhenCloudDiscoveryEnabled_ConsulDiscoveryClientEnabled() {
		TestPropertyValues.of("spring.cloud.discovery.enabled=true", "spring.cloud.consul.discovery.enabled=true")
			.applyTo(this.context);
		setupContext();
		assertBeanIsPresent(ConsulDiscoveryProperties.class);
		assertBeanIsPresent(ConsulDiscoveryClient.class);
	}

	@Test
	public void consulConfigNotLoadedWhenCloudDiscoveryEnabled_ConsulDiscoveryClientDisabled() {
		TestPropertyValues.of("spring.cloud.discovery.enabled=true", "spring.cloud.consul.discovery.enabled=false")
			.applyTo(this.context);
		setupContext();
		assertBeanNotPresent(ConsulDiscoveryProperties.class);
		assertBeanNotPresent(ConsulDiscoveryClient.class);
		assertBeanNotPresent(HeartbeatProperties.class);
	}

	@Test
	public void consulConfigNotLoadedWhenCloudDiscoveryDisabled_ConsulDiscoveryClientEnabled() {
		TestPropertyValues.of("spring.cloud.discovery.enabled=false", "spring.cloud.consul.discovery.enabled=true")
			.applyTo(this.context);
		setupContext();
		assertBeanNotPresent(ConsulDiscoveryProperties.class);
		assertBeanNotPresent(ConsulDiscoveryClient.class);
		assertBeanNotPresent(HeartbeatProperties.class);
	}

	private void setupContext(Class<?>... config) {
		ConfigurationPropertySources.attach(this.context.getEnvironment());
		this.context.register(UtilAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class,
				ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class);
		for (Class<?> value : config) {
			this.context.register(value);
		}
		this.context.refresh();
	}

	private void assertBeanNotPresent(Class beanClass) {
		try {
			context.getBean(beanClass);
			fail("Bean of type " + beanClass + " should not have been created.");
		}
		catch (NoSuchBeanDefinitionException exception) {
			// expected exception
		}
	}

	private void assertBeanIsPresent(Class beanClass) {
		try {
			context.getBean(beanClass);
		}
		catch (NoSuchBeanDefinitionException exception) {
			fail("Bean of type " + beanClass + " should have been created.");
		}
	}

}
