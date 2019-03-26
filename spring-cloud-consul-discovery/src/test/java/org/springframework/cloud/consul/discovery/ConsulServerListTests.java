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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.netflix.client.config.DefaultClientConfigImpl;
import org.junit.Test;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulServerListTests {

	private final String name = "consulServerListTestsService";

	@Test
	public void tagsWork() {
		ConsulClient consul = new ConsulClient();

		NewService nonTagged = createService("NonTagged", 8080, null);

		String tag = "mytag";
		NewService tagged = createService("Tagged", 9080, Arrays.asList(tag));

		String zone = "myzone";
		NewService withZone = createService("WithZone", 10080,
				Arrays.asList("zone=" + zone));

		String group = "test";
		NewService withGroup = createService("WithGroup", 11080,
				Arrays.asList("group=" + group));

		try {
			consul.agentServiceRegister(nonTagged);
			consul.agentServiceRegister(tagged);
			consul.agentServiceRegister(withZone);
			consul.agentServiceRegister(withGroup);

			InetUtils inetUtils = new InetUtils(new InetUtilsProperties());
			DefaultClientConfigImpl config = new DefaultClientConfigImpl();
			config.setClientName(this.name);

			ConsulServerList serverList = new ConsulServerList(consul,
					new ConsulDiscoveryProperties(inetUtils));
			serverList.initWithNiwsConfig(config);

			List<ConsulServer> servers = serverList.getInitialListOfServers();
			assertThat(servers).as("servers was wrong size").hasSize(4);

			int serverWithZoneCount = 0;
			for (ConsulServer server : servers) {
				if (server.getMetadata().containsKey("zone")) {
					serverWithZoneCount++;
					assertThat(server.getZone()).as("server was wrong zone")
							.isEqualTo(zone);
				}
				else {
					assertThat(server.getZone()).as("server was wrong zone")
							.isEqualTo(ConsulServer.UNKNOWN_ZONE);
				}
			}
			assertThat(serverWithZoneCount).as("server was wrong zone").isEqualTo(1);

			serverList = new ConsulServerList(consul,
					getProperties(this.name, tag, inetUtils));
			serverList.initWithNiwsConfig(config);

			servers = serverList.getInitialListOfServers();
			assertThat(servers).as("servers was wrong size").hasSize(1);
			ConsulServer server = servers.get(0);
			assertThat(server.getPort()).as("server was wrong").isEqualTo(9080);

			// test server group
			serverList = new ConsulServerList(consul,
					getProperties(this.name, "group=" + group, inetUtils));
			serverList.initWithNiwsConfig(config);

			servers = serverList.getInitialListOfServers();
			assertThat(servers).as("servers was wrong size").hasSize(1);
			server = servers.get(0);
			assertThat(server.getPort()).as("server was wrong").isEqualTo(11080);
			assertThat(server.getMetaInfo().getServerGroup()).as("server group was wrong")
					.isEqualTo(group);
		}
		finally {
			consul.agentServiceDeregister(nonTagged.getId());
			consul.agentServiceDeregister(tagged.getId());
			consul.agentServiceDeregister(withZone.getId());
			consul.agentServiceDeregister(withGroup.getId());
		}
	}

	private ConsulDiscoveryProperties getProperties(String name, String tag,
			InetUtils inetUtils) {
		ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(inetUtils);
		HashMap<String, String> map = new HashMap<>();
		map.put(name, tag);
		properties.setServerListQueryTags(map);
		return properties;
	}

	private NewService createService(String id, int port, List<String> tags) {
		NewService service = new NewService();
		service.setName(this.name);
		service.setId(this.name + id);
		service.setAddress("localhost");
		service.setPort(port);
		if (tags != null) {
			service.setTags(tags);
		}
		return service;
	}

}
