/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.consul.hcl;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

public class HclPropertySourceLoaderTests {

	private HclPropertySourceLoader loader = new HclPropertySourceLoader();

	@Test
	void getFileExtensions() {
		assertThat(this.loader.getFileExtensions()).isEqualTo(new String[] { "hcl" });
	}

	@Test
	void loadProperties() throws Exception {
		List<PropertySource<?>> loaded = this.loader.load("test.hcl",
				new ClassPathResource("test.hcl", getClass()));
		PropertySource<?> source = loaded.get(0);
		assertThat(source.getProperty("variable.region.description"))
				.isEqualTo("This is the location where the Linode instance is deployed.");
	}
}
