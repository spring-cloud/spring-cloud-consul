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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.acl.model.AclType;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.ecwid.consul.v1.agent.model.NewService;
import com.netflix.client.config.DefaultClientConfigImpl;
import org.junit.Test;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class ConsulServerListTests {
    private final String name = "consulServerListTestsService";

    //@Test
    public void tagsWork() {
        ConsulClient consul = new ConsulClient();

        NewService nonTagged = createService("NonTagged", 8080, null);

        String tag = "mytag";
        NewService tagged = createService("Tagged", 9080, Arrays.asList(tag));

        String zone = "myzone";
        NewService withZone = createService("WithZone", 10080, Arrays.asList("zone=" + zone));

        String group = "test";
        NewService withGroup = createService("WithGroup", 11080, Arrays.asList("group=" + group));

        try {
            consul.agentServiceRegister(nonTagged);
            consul.agentServiceRegister(tagged);
            consul.agentServiceRegister(withZone);
            consul.agentServiceRegister(withGroup);

            InetUtils inetUtils = new InetUtils(new InetUtilsProperties());
            DefaultClientConfigImpl config = new DefaultClientConfigImpl();
            config.setClientName(name);

            ConsulServerList serverList = new ConsulServerList(consul, new ConsulDiscoveryProperties(inetUtils));
            serverList.initWithNiwsConfig(config);

            List<ConsulServer> servers = serverList.getInitialListOfServers();
            assertThat("servers was wrong size", servers, hasSize(4));

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

            // test server group
            serverList = new ConsulServerList(consul, getProperties(name, "group=" + group, inetUtils));
            serverList.initWithNiwsConfig(config);

            servers = serverList.getInitialListOfServers();
            assertThat("servers was wrong size", servers, hasSize(1));
            server = servers.get(0);
            assertThat("server was wrong", server.getPort(), is(11080));
            assertThat("server group was wrong", server.getMetaInfo().getServerGroup(), is(group));
        } finally {
            consul.agentServiceDeregister(nonTagged.getId());
            consul.agentServiceDeregister(tagged.getId());
            consul.agentServiceDeregister(withZone.getId());
            consul.agentServiceDeregister(withGroup.getId());
        }
    }

    @Test
    public void testWithAclToken() {
        ConsulClient consul = new ConsulClient();

        NewService authorizedService = createService("authorizedService", 8001, null);
        NewService unauthorizedService = createService("unauthorizedService", 8002, null);

        String manageToken = "23mofang";
        String clientToken = null;
        try {
            final NewAcl clientAcl = new NewAcl();
            clientAcl.setName("acl_test");
            clientAcl.setType(AclType.CLIENT);
            clientAcl.setRules("service \"authorizedService\" {\n" +
                    "  policy=\"write\"\n" +
                    "}\n" +
                    "\n" +
                    "service \"unauthorizedService\" {\n" +
                    "  policy=\"deny\"\n" +
                    "}");

            clientToken = consul.aclCreate(clientAcl, manageToken).getValue();

            consul.agentServiceRegister(authorizedService, manageToken);
            consul.agentServiceRegister(unauthorizedService, manageToken);
            {

                // limited token get server list
                ConsulDiscoveryProperties properties1 = getAclTokenProperties(clientToken);
                ConsulServerList consulServerList = new ConsulServerList(consul, properties1);
                final List<ConsulServer> listOfServers = consulServerList.getUpdatedListOfServers();
                assertThat("servers was wrong size", listOfServers, hasSize(1));
            }

            {
                // unlimited token get server list
                ConsulDiscoveryProperties properties2 = getAclTokenProperties(manageToken);
                ConsulServerList consulServerList2 = new ConsulServerList(consul, properties2);
                final List<ConsulServer> listOfServers2 = consulServerList2.getUpdatedListOfServers();
                assertThat("servers was wrong size", listOfServers2, hasSize(2));
            }

        } finally {
            consul.agentServiceDeregister(authorizedService.getId());
            consul.agentServiceDeregister(unauthorizedService.getId());
            if (clientToken != null) {
                consul.aclDestroy(clientToken, manageToken);
            }
        }
    }

    private ConsulDiscoveryProperties getAclTokenProperties(String aclToken) {
        ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(new InetUtils(new InetUtilsProperties()));
        properties.setAclToken(aclToken);
        return properties;
    }

    private ConsulDiscoveryProperties getProperties(String name, String tag, InetUtils inetUtils) {
        ConsulDiscoveryProperties properties = new ConsulDiscoveryProperties(inetUtils);
        HashMap<String, String> map = new HashMap<>();
        map.put(name, tag);
        properties.setServerListQueryTags(map);
        return properties;
    }

    private NewService createService(String id, int port, List<String> tags) {
        NewService service = new NewService();
        service.setName(name);
        service.setId(name + id);
        service.setAddress("localhost");
        service.setPort(port);
        if (tags != null) {
            service.setTags(tags);
        }
        return service;
    }
}
