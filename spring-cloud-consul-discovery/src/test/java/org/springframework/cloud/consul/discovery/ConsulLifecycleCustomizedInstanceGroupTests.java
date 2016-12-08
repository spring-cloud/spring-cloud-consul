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

package org.springframework.cloud.consul.discovery;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.netflix.client.config.DefaultClientConfigImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Jin Zhang
 */
@Deprecated
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestPropsConfig.class,
	properties = { "spring.application.name=myTestService-WithGroup",
		"spring.cloud.consul.discovery.instanceId=myTestService1-WithGroup",
		"spring.cloud.consul.discovery.instanceGroup=test"},
		webEnvironment = RANDOM_PORT)
public class ConsulLifecycleCustomizedInstanceGroupTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-WithGroup");
		assertNotNull("service was null", service);
		assertNotEquals("service port is 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService1-WithGroup", service.getId());
		assertTrue("service group was wrong", service.getTags().contains("group=test"));

		ConsulServerList serverList = new ConsulServerList(consul, properties);
		DefaultClientConfigImpl config = new DefaultClientConfigImpl();
		config.setClientName("myTestService-WithGroup");
		serverList.initWithNiwsConfig(config);

		List<ConsulServer> servers = serverList.getInitialListOfServers();
		assertEquals("servers was wrong size", 1, servers.size());
		assertEquals("service group was wrong", "test", servers.get(0).getMetaInfo().getServerGroup());
	}
}
