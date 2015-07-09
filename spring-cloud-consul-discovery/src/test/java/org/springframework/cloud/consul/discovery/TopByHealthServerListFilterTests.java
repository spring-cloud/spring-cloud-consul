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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.UUID;

import com.netflix.client.config.DefaultClientConfigImpl;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.discovery.filters.TopByHealthServerListFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TestConfig.class)
@IntegrationTest({ "server.port=0", "spring.application.name=myTestService" })
@WebAppConfiguration
@Slf4j
public class TopByHealthServerListFilterTests {
	@Autowired
	ConsulClient consul;

	String serviceName;
	ServerList serverList;
	ServerListFilter filter;

	@Before
	public void setUp() throws Exception {
		serviceName = "serviceName3_" + UUID.randomUUID().toString();
		filter = new TopByHealthServerListFilter(consul);
		serverList = getConsulServerList(serviceName);
	}

	private ConsulServerList getConsulServerList(String serviceName) {
		ConsulServerList consulServerList = new ConsulServerList(consul, new ConsulDiscoveryProperties());
		DefaultClientConfigImpl clientConfig = new DefaultClientConfigImpl();
		clientConfig.setClientName(serviceName);
		consulServerList.initWithNiwsConfig (clientConfig);
		return consulServerList;
	}

	@Test
	public void noInstanceAllGood() {
		assertEquals(0, listOfServers(filter).size());
	}

	@Test
	public void singleInstanceAllGood() {
		NewService serviceInstance = createInstance();
		register(serviceInstance);
		assertEquals(1, listOfServers(filter).size());
	}

    @Test
    public void singleInstanceWarn() {
        NewService serviceInstance = createInstance();
        register(serviceInstance);
        markWarn(serviceInstance);
        assertEquals(1, listOfServers(filter).size());
    }

	@Test
	public void singleInstanceExpiredTtl() {
		NewService serviceInstance = createInstance();
        setExpiredAlready(serviceInstance);
        register(serviceInstance);
		assertEquals(1, listOfServers(filter).size());
	}

	@Test
	public void singleInstanceCritical() {
		NewService serviceInstance = createInstance();
		register(serviceInstance);
		markCritical(serviceInstance);
		assertEquals(1, listOfServers(filter).size());
	}

	@Test
	public void twoInstancesOneExpiredTtl() {
		// the orange (expired) instance
		NewService expiredServiceInstance = createInstance();
        setExpiredAlready(expiredServiceInstance);
        register(expiredServiceInstance);
		// and the green instance
        NewService greenInstanceId = createInstance();
        register(greenInstanceId);
        List<Server> servers = listOfServers(filter);
        assertEquals(1, servers.size());
        assertEquals(greenInstanceId.getId(), servers.get(0).getMetaInfo().getInstanceId());
	}

    private void setExpiredAlready(NewService tobeExpiredServiceInstance) {
        tobeExpiredServiceInstance.getCheck().setTtl("1ms");
        log.debug("making {} with small ttl" + tobeExpiredServiceInstance.getId());
    }

    @Test
	public void twoInstancesOneCritical() {
		// the orange (expired) instance
		NewService serviceInstance = createInstance();
		register(serviceInstance);
		markCritical(serviceInstance);
		// and the green instance
        NewService greenInstanceId = createInstance();
        register(greenInstanceId);
        List<Server> servers = listOfServers(filter);
        assertEquals(1, servers.size());
        assertEquals(greenInstanceId.getId(), servers.get(0).getMetaInfo().getInstanceId());
	}

    @Test
    public void twoInstancesOneWarning() {
        // the orange (expired) instance
        NewService yellowServiceInstance = createInstance();
        register(yellowServiceInstance);
        markWarn(yellowServiceInstance);
        // and the green instance
        NewService greenInstanceId = createInstance();
        register(greenInstanceId);
        List<Server> servers = listOfServers(filter);
        assertEquals(1, servers.size());
        String promotedInstanceId = servers.get(0).getMetaInfo().getInstanceId();
        assertNotEquals(yellowServiceInstance.getId(), promotedInstanceId);
        assertEquals(greenInstanceId.getId(), promotedInstanceId);

    }

	private void register(NewService serviceInstance) {
		consul.agentServiceRegister(serviceInstance);
		sleep();
        markPassed(serviceInstance);
        sleep();
	}

	private void markCritical(NewService serviceInstance) {
		consul.agentCheckFail("service:" + serviceInstance.getId());
        log.debug("making {} critical"+serviceInstance.getId());
        sleep();
	}

	private void markWarn(NewService serviceInstance) {
		consul.agentCheckWarn("service:" + serviceInstance.getId());
        log.debug("making {} warn", serviceInstance.getId());
        sleep();
	}

    private void markPassed(NewService serviceInstance) {
        consul.agentCheckPass("service:" + serviceInstance.getId());
        log.debug("making {} pass", serviceInstance.getId());
        sleep();
    }

	private <T extends Server> List<T> listOfServers(ServerListFilter<T> filter) {
		List<T> servers = serverList.getUpdatedListOfServers();
		log.debug("List of Servers obtained from Discovery client: {}", servers);
		servers = filter.getFilteredListOfServers(servers);
		log.debug("Filtered List of Servers obtained from Discovery client: {}", servers);
		return (servers);
	}

	private NewService createInstance() {
		NewService service = new NewService();
		service.setId("serviceInstanceId_" + UUID.randomUUID().toString().substring(0,3));
		service.setName(serviceName);
		Integer port = 17854;
		service.setPort(port);
		// service.setTags(createTags());
		NewService.Check check = new NewService.Check();
		check.setTtl(122 + "s");
		service.setCheck(check);
		return service;
	}

	private void sleep() {
		try {
			Thread.sleep(134);
		}
		catch (InterruptedException ignored) {
		}
	}
}
