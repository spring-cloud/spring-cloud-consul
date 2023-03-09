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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Daniel Wu In some cases the profile is not needed, we can cut 50% pressure to
 * Consul in that case. This test case mainly focus on the behaviro of
 * <code>spring.cloud.consul.config.profileEnabled</code>.
 */
public class ConsulPropertySourcesTests {

	@Test
	public void testProfileEnabledByDefault() {
		List<String> profiles = new ArrayList<>();
		profiles.add("default");
		ConsulConfigProperties properties = new ConsulConfigProperties();
		properties.setName("myapp");
		ConsulPropertySources sources = new ConsulPropertySources(properties, mock(Log.class));
		List<ConsulPropertySources.Context> contexts = sources.generateAutomaticContexts(profiles, false);
		List<ConsulPropertySources.Context> expected = new ArrayList<>();
		expected.add(new ConsulPropertySources.Context("config/application/"));
		expected.add(new ConsulPropertySources.Context("config/application,default/"));
		expected.add(new ConsulPropertySources.Context("config/myapp/"));
		expected.add(new ConsulPropertySources.Context("config/myapp,default/"));
		assertThat(contexts).usingRecursiveFieldByFieldElementComparatorOnFields("path").isEqualTo(expected);
	}

	@Test
	public void testProfileDisabled() {
		List<String> profiles = new ArrayList<>();
		profiles.add("default");
		ConsulConfigProperties properties = new ConsulConfigProperties();
		properties.setName("myapp");
		properties.setProfileEnabled(false);
		ConsulPropertySources sources = new ConsulPropertySources(properties, mock(Log.class));
		List<ConsulPropertySources.Context> contexts = sources.generateAutomaticContexts(profiles, false);
		List<ConsulPropertySources.Context> expected = new ArrayList<>();
		expected.add(new ConsulPropertySources.Context("config/application/"));
		expected.add(new ConsulPropertySources.Context("config/myapp/"));
		assertThat(contexts).usingRecursiveFieldByFieldElementComparatorOnFields("path").isEqualTo(expected);
	}

}
