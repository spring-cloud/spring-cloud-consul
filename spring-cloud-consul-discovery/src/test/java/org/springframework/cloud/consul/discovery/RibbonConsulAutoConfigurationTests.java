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

package org.springframework.cloud.consul.discovery;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RibbonConsulAutoConfiguration}.
 *
 * @author Flora Kalisa
 */

public class RibbonConsulAutoConfigurationTests {

	ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(RibbonAutoConfiguration.class,
					RibbonConsulAutoConfiguration.class));

	@Test
	public void shouldWorkWithDefaults() {
		contextRunner.run(context -> assertThat(context)
				.hasSingleBean(RibbonConsulAutoConfiguration.class));
	}

	@Test
	public void shouldNotHaveRibbonConsulAutoConfigWhenConsulRibbonDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.ribbon.enabled=false")
				.run(context -> assertThat(context)
						.doesNotHaveBean(RibbonConsulAutoConfiguration.class));
	}

	@Test
	public void shouldNotHaveRibbonConsulAutoConfigWhenConsulDiscoveryDisabled() {
		contextRunner.withPropertyValues("spring.cloud.consul.discovery.enabled=false")
				.run(context -> assertThat(context)
						.doesNotHaveBean(RibbonConsulAutoConfiguration.class));
	}

}
