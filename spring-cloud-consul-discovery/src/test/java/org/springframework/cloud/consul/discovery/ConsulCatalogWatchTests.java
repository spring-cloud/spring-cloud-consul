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

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulCatalogWatchTests {

	@Test
	public void isRunningReportsCorrectly() {
		ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
		ConsulCatalogWatch watch = new ConsulCatalogWatch(properties, null) {
			@Override
			public void catalogServicesWatch() {
				// do nothing
			}
		};
		assertThat(watch.isRunning()).isFalse();
		watch.start();
		assertThat(watch.isRunning()).isTrue();
		watch.stop();
		assertThat(watch.isRunning()).isFalse();
	}

}
