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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Semenkov Alexey
 */
public class ConsulServerUtilsTest {

	@Test
	public void testAddressFormat() {
		String s1 = ConsulServerUtils.fixIPv6Address("fc00:ec:cd::242:ac11:c");
		assertThat(s1).isEqualTo("[fc00:ec:cd:0:0:242:ac11:c]");

		String s2 = ConsulServerUtils.fixIPv6Address("[fc00:ec:cd::242:ac11:c]");
		assertThat(s2).isEqualTo("[fc00:ec:cd:0:0:242:ac11:c]");

		String s3 = ConsulServerUtils.fixIPv6Address("192.168.0.1");
		assertThat(s3).isEqualTo("192.168.0.1");

		String s4 = ConsulServerUtils.fixIPv6Address("projects.spring.io");
		assertThat(s4).isEqualTo("projects.spring.io");

		String s5 = ConsulServerUtils.fixIPv6Address("veryLongHostName");
		assertThat(s5).isEqualTo("veryLongHostName");

	}

}
