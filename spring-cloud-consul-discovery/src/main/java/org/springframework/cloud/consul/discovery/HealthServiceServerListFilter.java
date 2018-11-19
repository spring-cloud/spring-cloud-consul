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

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ServerList implementation that filters ConsulServers based on if all their Health Checks are PASSING.
 * @author Spencer Gibb
 */
public class HealthServiceServerListFilter implements ServerListFilter<Server> {
	private static final Log log = LogFactory.getLog(HealthServiceServerListFilter.class);

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		List<Server> filtered = new ArrayList<>();

		for (Server server : servers) {
			if (server instanceof ConsulServer) {
				ConsulServer consulServer = (ConsulServer) server;

				if (consulServer.isPassingChecks()) {
					filtered.add(server);
				}

			} else {
			    if (log.isDebugEnabled()) {
					log.debug("Unable to determine aliveness of server type " + server.getClass() + ", " + server);
				}
				filtered.add(server);
			}
		}

		return filtered;
	}
}
