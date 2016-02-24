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

package org.springframework.cloud.consul.discovery;

import java.util.ArrayList;
import java.util.List;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.netflix.loadbalancer.Server;

import org.junit.Test;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Spencer Gibb
 */
public class HealthServiceServerListFilterTests {

	@Test
	public void testGetFilteredListOfServers() {
		HealthServiceServerListFilter filter = new HealthServiceServerListFilter();

		ArrayList<Server> servers = new ArrayList<>();
		servers.add(newServer(PASSING));
		servers.add(newServer(PASSING));
		servers.add(newServer(CRITICAL));
		servers.add(newServer(WARNING));

		List<Server> filtered = filter.getFilteredListOfServers(servers);
		assertThat("wrong # of filtered servers", filtered, hasSize(2));
	}

	private ConsulServer newServer(Check.CheckStatus checkStatus) {
		HealthService healthService = new HealthService();
		HealthService.Node node = new HealthService.Node();
		node.setAddress("nodeaddr"+checkStatus.name());
		node.setNode("nodenode"+checkStatus.name());
		healthService.setNode(node);
		HealthService.Service service = new HealthService.Service();
		service.setAddress("serviceaddr"+checkStatus.name());
		service.setId("serviceid"+checkStatus.name());
		service.setPort(8080);
		service.setService("serviceservice"+checkStatus.name());
		healthService.setService(service);
		ArrayList<Check> checks = new ArrayList<>();
		Check check = new Check();
		check.setStatus(checkStatus);
		checks.add(check);
		healthService.setChecks(checks);
		return new ConsulServer(healthService);
	}
}
