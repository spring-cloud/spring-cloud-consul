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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.health.model.Check;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 * @author Venil Noronha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TestPropsConfig.class)
@WebIntegrationTest(value = { "spring.application.name=myTestService-B",
		"spring.cloud.consul.discovery.instanceId=myTestService1-B",
		"spring.cloud.consul.discovery.port=4452",
		"spring.cloud.consul.discovery.hostname=myhost",
		"spring.cloud.consul.discovery.ipAddress=10.0.0.1",
		"spring.cloud.consul.discovery.registerHealthCheck=false",
		"spring.cloud.consul.discovery.failFast=false" }, randomPort = true)
public class ConsulLifecycleCustomizedPropsTests {

	@Autowired
	ConsulLifecycle lifecycle;

	@Autowired
	ConsulClient consul;

	@Autowired
	ApplicationContext context;

	@Autowired
	ConsulDiscoveryProperties properties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-B");
		assertThat("service was null", service, is(notNullValue()));
		assertThat("service port is discovery port", 4452, equalTo(service.getPort()));
		assertThat("service id was wrong", "myTestService1-B", equalTo(service.getId()));
		assertThat("service name was wrong", "myTestService-B", equalTo(service.getService()));
		assertThat("property hostname was wrong", "myhost", equalTo(this.properties.getHostname()));
		assertThat("property ipAddress was wrong", "10.0.0.1", equalTo(this.properties.getIpAddress()));
		assertThat("service address was wrong", "myhost", equalTo(service.getAddress()));

		Response<List<Check>> checkResponse = consul.getHealthChecksForService("myTestService-B", QueryParams.DEFAULT);
		List<Check> checks = checkResponse.getValue();
		assertThat("checks was wrong size", checks, hasSize(0));
	}

	@Test
	public void testFailFastDisabled() {
		assertFalse("property failFast was wrong", this.properties.isFailFast());
	}

}

@Configuration
@EnableAutoConfiguration
@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
class TestPropsConfig {

}
