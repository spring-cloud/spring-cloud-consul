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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netflix.loadbalancer.Server;
import org.junit.Test;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.consul.discovery.ConsulServerTest.newServer;

/**
 * @author Spencer Gibb
 */
public class HealthServiceServerListFilterTests {

	@Test
	public void testGetFilteredListOfServers() {
		HealthServiceServerListFilter filter = new HealthServiceServerListFilter();

		ArrayList<Server> servers = new ArrayList<>();
		servers.add(newServer(PASSING, Collections.singleton(PASSING)));
		servers.add(newServer(PASSING, Collections.singleton(PASSING)));
		servers.add(newServer(CRITICAL, Collections.singleton(PASSING)));
		servers.add(newServer(WARNING, Collections.singleton(PASSING)));

		List<Server> filtered = filter.getFilteredListOfServers(servers);
		assertThat(filtered).as("wrong # of filtered servers").hasSize(2);
	}

}
