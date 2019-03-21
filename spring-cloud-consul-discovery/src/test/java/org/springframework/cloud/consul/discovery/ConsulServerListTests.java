/*
 * Copyright 2013-2016 the original author or authors.
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

import org.junit.Test;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.netflix.client.config.DefaultClientConfigImpl;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulServerListTests {

	@Test
	public void tagsWork() {
		String name = "consulServerListTestsService";
		ConsulClient consul = new ConsulClient();
		NewService nonTagged = new NewService();
		nonTagged.setAddress("localhost");
		nonTagged.setId(name+"NonTagged");
		nonTagged.setName(name);
		nonTagged.setPort(8080);

		NewService tagged = new NewService();
		tagged.setAddress("localhost");
		tagged.setId(name+"Tagged");
		tagged.setName(name);
		tagged.setPort(9080);
		String tag = "mytag";
		tagged.setTags(Arrays.asList(tag));

		NewService withZone = new NewService();
		withZone.setAddress("localhost");
		withZone.setId(name+"WithZone");
		withZone.setName(name);
		withZone.setPort(10080);
		String zone = "myzone";
		withZone.setTags(Arrays.asList("zone=" + zone));

		try {
			consul.agentServiceRegister(nonTagged);
			consul.agentServiceRegister(tagged);
			consul.agentServiceRegister(withZone);

			InetUtils inetUtils = new InetUtils(new InetUtilsProperties());
			DefaultClientConfigImpl config = new DefaultClientConfigImpl();
			config.setClientName(tagged.getName());

			ConsulServerList serverList = new ConsulServerList(consul, new ConsulDiscoveryProperties(inetUtils));
			serverList.initWithNiwsConfig(config);

			List<ConsulServer> servers = serverList.getInitialListOfServers();
			assertThat("servers was wrong size", servers, hasSize(3));

			int serverWithZoneCount = 0;
			for (ConsulServer server : servers) {
				if (server.getMetadata().containsKey("zone")) {
					serverWithZoneCount++;
					assertThat("server was wrong zone", server.getZone(), is(zone));
				} else {
					assertThat("server was wrong zone", server.getZone(), is(ConsulServer.UNKNOWN_ZONE));
				}
			}
			assertThat("server was wrong zone", serverWithZoneCount, is(1));

			serverList = new ConsulServerList(consul, getProperties(name, tag, inetUtils));
			serverList.initWithNiwsConfig(config);

			servers = serverList.getInitialListOfServers();
			assertThat("servers was wrong size", servers, hasSize(1));
			ConsulServer server = servers.get(0);
			assertThat("server was wrong", server.getPort(), is(9080));
		} finally {
			consul.agentServiceDeregister(nonTagged.getId());
			consul.agentServiceDeregister(tagged.getId());
			consul.agentServiceDeregister(withZone.getId());
		}
	}

	private ConsulDiscoveryProperties getProperties(String name, String tag, InetUtils inetUtils) {
		ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(inetUtils);
		HashMap<String, String> map = new HashMap<>();
		map.put(name, tag);
		properties.setServerListQueryTags(map);
		return properties;
	}
}
