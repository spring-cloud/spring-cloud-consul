/*
 * Copyright 2013-2015 the original author or authors.
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

package org.springframework.cloud.consul.discovery.filters;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.WARNING;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.consul.discovery.ConsulServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.health.model.Check;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;

/**
 * Collect the best service instances.
 *
 * Given the order of preference: passing/warning/fail;
 * Considering lack of checks as warning;
 * And taking the lowest status check as the status,
 * Collect all the service instances having that status.
 *
 * @author nmarasoiu on 12.03.2015.
 */
@Slf4j
@AllArgsConstructor
public class TopByHealthServerListFilter implements ServerListFilter<Server> {
	private ConsulClient client;

	/**
	 * Keep green service instances. If empty, keep yellow instances (any non critical).
	 * If empty, return empty.
	 */

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		log.debug("Before filtering: servers={}", servers);
		PriorityQueue<Server> heap = new PriorityQueue<>(1 + servers.size(),
				new Comparator<Server>() {
					@Override
					public int compare(Server o1, Server o2) {
						return compareByHealth(o1, o2);
					}
				});
		heap.addAll(servers);
		log.debug("heap={}", heap);

		List<Server> topInstances = new ArrayList<>();
		for (Server topInstance = heap.peek(); heap.peek() != null
				&& compareByHealth(topInstance, heap.peek()) == 0;) {
			topInstances.add(heap.remove());
		}
		return topInstances;
	}

	private int compareByHealth(Server o1, Server o2) {
		return getStatus(o1).compareTo(getStatus(o2));
	}

	private Check.CheckStatus getStatus(Server instance) {
		return cast(instance).getLowestCheckStatus();
	}

	private ConsulServer cast(Server server) {
		return ConsulServer.class.cast(server);
	}
}
